package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;
import sample.dto.MessageDto;
import sample.dto.Receiver;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.model.UserRoomEntranceTime;
import sample.repository.MessageRepository;
import sample.repository.UserRoomEntranceTimeRepository;
import sample.service.MessageConstructor;
import sample.service.MessageService;
import sample.service.RoomService;
import sample.service.UserService;
import sample.utils.Ranks;
import sample.utils.crypto.Cryptography;
import sample.utils.validation.GCValidator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MessageServiceImpl implements MessageService {

    private Map<Message, Set<User>> receiversByMessage = new ConcurrentHashMap<>();

    private final UserRoomEntranceTimeRepository entranceTimeRepository;
    private final SessionMessageSender sessionMessageSender;
    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final UserService userService;


    @Autowired
    public MessageServiceImpl(UserRoomEntranceTimeRepository entranceTimeRepository,
                              SessionMessageSender sessionMessageSender,
                              MessageRepository messageRepository,
                              RoomService roomService,
                              UserService userService) {
        this.entranceTimeRepository = entranceTimeRepository;
        this.sessionMessageSender = sessionMessageSender;
        this.messageRepository = messageRepository;
        this.roomService = roomService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Message createMessage(Message message) {
        GCValidator.validateObject(message);
        message.setCreated(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public Message updateMessage(Message message) {
        GCValidator.validateObject(message);
        if (messageRepository.exists(message.getId())) {
            return messageRepository.save(message);
        } else throw new NoSuchElementException("Entity not found in DB");
    }

    @Override
    public List<Receiver> getReceivers() {
        List<Receiver> receiversList = new ArrayList<>();
        receiversByMessage.forEach((message, users) -> createReceiverEntry(receiversList, message, users));
        return receiversList;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void sendMessage(Room room, Message message, Map<UUID, WebSocketSession> sessionByUUID) {
        GCValidator.validateObject(room);
        GCValidator.validateObject(message);

        Room createdRoom = roomService.findOrCreateRoom(room.getName());
        createdRoom.getMessages().add(message);

        Set<User> allUsersInRoom = userService.getAllUsersInRoom(createdRoom);

        if (message.isSecret()) {
            sendEncryptedMessage(message, sessionByUUID, allUsersInRoom);
        } else {
            sendPublicMessage(message, sessionByUUID, allUsersInRoom);
        }
        receiversByMessage.put(message, allUsersInRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public void showMessagesForUserInRoom(User user, Room room, Map<UUID, WebSocketSession> sessionByUUID) {
        GCValidator.validateObject(user);
        GCValidator.validateObject(room);

        Room createdRoom = roomService.findOrCreateRoom(room.getName());

        Set<Message> secretMessages = messageRepository.findAllBySecret(true);

        UserRoomEntranceTime roomEntranceTime = entranceTimeRepository.findByRoomNameAndUserUuid(room.getName(), user.getUuid());

        Stream<MessageDto> secretMessagesDto = getSecretMessagesDtoStream(user, secretMessages, roomEntranceTime);
        Stream<MessageDto> publicMessagesDto = getPublicMessagesDtoStream(createdRoom, roomEntranceTime);

        Set<MessageDto> combinedSet = Stream
                .concat(secretMessagesDto, publicMessagesDto)
                .collect(Collectors.toSet());

        sessionMessageSender.sendObjectToSession(combinedSet, sessionByUUID).accept(user);
    }

    @Override
    public String salute(String name, String hash) {
        Optional<Integer> rank = Ranks.getRank(name, hash);
        String result = "Unauthorized";

        if (rank.isPresent()) {
            result = "You are " + Ranks.getRankName(rank.get());
        }
        return result;
    }

    private void createReceiverEntry(List<Receiver> receiversList, Message message, Set<User> users) {
        Receiver receiver = Receiver.builder()
                .messageId(message.getId())
                .recipients(users.stream().map(User::getName).collect(Collectors.toList()))
                .build();
        receiversList.add(receiver);
    }

    private void sendPublicMessage(Message message, Map<UUID, WebSocketSession> sessionByUUID, Set<User> allUsersInRoom) {
        Message persistedMessage = createMessage(message);
        MessageDto dto = MessageConstructor.toDto(persistedMessage);
        allUsersInRoom
                .forEach(sessionMessageSender.sendObjectToSession(dto, sessionByUUID));
    }

    private void sendEncryptedMessage(Message message, Map<UUID, WebSocketSession> sessionByUUID, Set<User> allUsersInRoom) {
        Message encrypted = MessageConstructor.getMessageWithEncryptedText(message);
        Message persistedMessage = createMessage(encrypted);

        MessageDto dto = MessageConstructor.toDto(persistedMessage);

        allUsersInRoom
                .stream()
                .filter(filterByHigherRank(message))
                .forEach(sessionMessageSender.sendObjectToSession(dto, sessionByUUID));
    }

    private Stream<MessageDto> getPublicMessagesDtoStream(Room createdRoom, UserRoomEntranceTime roomEntranceTime) {
        return messageRepository
                .findAllByRoomName(createdRoom.getName()).stream()
                .filter(message -> !message.isSecret())
                .filter(messageCreatedAfter(roomEntranceTime))
                .map(MessageConstructor::toDto);
    }

    private Stream<MessageDto> getSecretMessagesDtoStream(User user, Set<Message> secretMessages, UserRoomEntranceTime roomEntranceTime) {
        return secretMessages.stream()
                .filter(messageCreatedAfter(roomEntranceTime))
                .peek(decryptMessageForRank(user.getRank()))
                .map(MessageConstructor::toDto);
    }

    private Consumer<Message> decryptMessageForRank(Integer rank) {
        return message -> {
            Integer messageSenderRank = message.getUser().getRank();
            if (rank.equals(messageSenderRank)) {
                message.setTextBytes(Cryptography.decryptMessageWithRank(message, rank));
            }
        };
    }

    private Predicate<Message> messageCreatedAfter(UserRoomEntranceTime roomEntranceTime) {
        return message -> message.getCreated().isAfter(roomEntranceTime.getTime());
    }

    private Predicate<User> filterByHigherRank(Message message) {
        return user -> user.getRank() >= message.getUser().getRank();
    }


}
