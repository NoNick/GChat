package sample.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.dto.Receiver;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.repository.MessageRepository;
import sample.service.MessageService;
import sample.service.RoomService;
import sample.service.UserService;
import sample.utils.SimpleValidator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private Map<Message, Set<User>> receiversByMessage = new ConcurrentHashMap<>();

    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, RoomService roomService, UserService userService, ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.roomService = roomService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Message createMessage(Message message) {
        SimpleValidator.validateObject(message, "Message must not be null");
        message.setCreated(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    public List<Receiver> getReceivers() {
        List<Receiver> receiversList = new ArrayList<>();

        receiversByMessage.forEach((message, users) -> {
            Receiver receiver = Receiver.builder()
                    .messageId(message.getId())
                    .recipients(users.stream().map(User::getName).collect(Collectors.toList()))
                    .build();
            receiversList.add(receiver);
        });
        return receiversList;

    }

    @Override
    @Transactional
    public void sendMessageToSubscribers(Message message, Map<UUID, WebSocketSession> sessionByUser) {

        Room createdRoom = roomService.findOrCreateRoom(message.getRoom().getName());

        createdRoom.getMessages().add(message);

        Set<User> allUsersInRoom = userService.getAllUsersInRoom(createdRoom);

        if (message.isSecret()) {
            allUsersInRoom
                    .stream()
                    .filter(filterByHigherRank(message))
                    .forEach(sendMessageToSessionConsumer(message, sessionByUser));

        } else allUsersInRoom
                .forEach(sendMessageToSessionConsumer(message, sessionByUser));

        createMessage(message);
        receiversByMessage.put(message, allUsersInRoom);
    }

    private Predicate<User> filterByHigherRank(Message message) {
        return user -> user.getRank() >= message.getUser().getRank();
    }

    private Consumer<User> sendMessageToSessionConsumer(Message message, Map<UUID, WebSocketSession> sessionByUser) {
        return user -> {
            WebSocketSession session = sessionByUser.get(user.getUuid());
            if (session != null && session.isOpen()) {
                sendMessageIntoSession(message, session);
            }
        };
    }

    private void sendMessageIntoSession(Message message, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
