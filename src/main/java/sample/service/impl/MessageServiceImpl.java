package sample.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MessageServiceImpl implements MessageService {

    private final ObjectMapper objectMapper;

    private final UserRoomEntranceTimeRepository entranceTimeRepository;
    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final UserService userService;
    private Map<Message, List<User>> receiversByMessage = new ConcurrentHashMap<>();


    @Autowired
    public MessageServiceImpl(UserRoomEntranceTimeRepository entranceTimeRepository,
                              MessageRepository messageRepository,
                              RoomService roomService,
                              UserService userService, ObjectMapper objectMapper) {
        this.entranceTimeRepository = entranceTimeRepository;
        this.messageRepository = messageRepository;
        this.roomService = roomService;
        this.userService = userService;
        this.objectMapper = objectMapper;
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
    public MessageDto sendMessage(Room room, Message message, Map<UUID, WebSocketSession> sessionByUUID) {
        GCValidator.validateObject(room);
        GCValidator.validateObject(message);
        GCValidator.validateObject(sessionByUUID);

        Room createdRoom = roomService.findOrCreateRoom(room.getName());
        createdRoom.getMessages().add(message);

        List<User> allUsersInRoom = userService.getAllUsersInRoom(createdRoom);

        MessageDto returningMessage;

        if (message.isSecret()) {
            returningMessage = sendEncryptedMessage(message, sessionByUUID, allUsersInRoom);
        } else {
            returningMessage = sendPublicMessage(message, sessionByUUID, allUsersInRoom);
        }
        receiversByMessage.put(message, allUsersInRoom);

        return returningMessage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> showMessagesForUserInRoom(User user, Room room, Map<UUID, WebSocketSession> sessionByUUID) {
        GCValidator.validateObject(user);
        GCValidator.validateObject(room);
        GCValidator.validateObject(sessionByUUID);

        Room createdRoom = roomService.findOrCreateRoom(room.getName());

        Set<Message> secretMessages = messageRepository.findAllBySecret(true);

        UserRoomEntranceTime roomEntranceTime = entranceTimeRepository.findByRoomNameAndUserUuid(room.getName(), user.getUuid());

        Stream<MessageDto> secretMessagesDto = getSecretMessagesDtoStream(user, secretMessages, roomEntranceTime);
        Stream<MessageDto> publicMessagesDto = getPublicMessagesDtoStream(createdRoom, roomEntranceTime);

        List<MessageDto> combinedSet = Stream
                .concat(secretMessagesDto, publicMessagesDto)
                .collect(Collectors.toList());

        sendObjectToSession(combinedSet, sessionByUUID).accept(user);
        return combinedSet;
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

    private void createReceiverEntry(List<Receiver> receiversList, Message message, List<User> users) {
        Receiver receiver = Receiver.builder()
                .messageId(message.getId())
                .recipients(users.stream().map(User::getName).collect(Collectors.toList()))
                .build();

        receiversList.add(receiver);
    }

    private MessageDto sendPublicMessage(Message message, Map<UUID, WebSocketSession> sessionByUUID, List<User> allUsersInRoom) {
        Message persistedMessage = createMessage(message);
        MessageDto dto = MessageConstructor.toDto(persistedMessage);

        allUsersInRoom
                .forEach(sendObjectToSession(dto, sessionByUUID));
        return dto;
    }

    private MessageDto sendEncryptedMessage(Message message, Map<UUID, WebSocketSession> sessionByUUID, List<User> allUsersInRoom) {
        Message encrypted = MessageConstructor.getMessageWithEncryptedText(message);
        Message persistedMessage = createMessage(encrypted);

        MessageDto dto = MessageConstructor.toDto(persistedMessage);

        allUsersInRoom
                .stream()
                .filter(filterByHigherRank(message))
                .forEach(sendObjectToSession(dto, sessionByUUID));
        return dto;
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
                .filter(message -> message.getUser().getRank() <= user.getRank())
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

    private Consumer<User> sendObjectToSession(Object object, Map<UUID, WebSocketSession> sessionByUser) {
        return user -> {
            WebSocketSession session = sessionByUser.get(user.getUuid());
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(object)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
