package sample.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.repository.MessageRepository;
import sample.service.MessageService;
import sample.utils.SimpleValidator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private Map<Room, Set<User>> usersByRoom = new ConcurrentHashMap<>();
    private Map<Room, Set<Message>> messagesByRoom = new ConcurrentHashMap<>();
    private Map<Message, Set<User>> receiversByMessage = new ConcurrentHashMap<>();

    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesByUser(User user) {
        SimpleValidator.validateObject(user, "User must not be null");
        return messageRepository.findAllByUser(user);
    }

    @Override
    @Transactional
    public Message createMessage(Message message) {
        SimpleValidator.validateObject(message, "Message must not be null");
        message.setCreated(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessagesInRoom(Room room) {
        SimpleValidator.validateObject(room, "Room must not be null");
        return messageRepository.findAllByRoom(room);
    }

    @Override
    @Transactional
    public void subscribeUser(Room room, User user, Map<User, WebSocketSession> sessionByUser) {

        Set<User> users = usersByRoom.computeIfAbsent(room, k -> new HashSet<>());

        if (usersByRoom.get(room).contains(user)) {
            return;
        }
        users.add(user);

        Message message = constructSubscribedMessage(user, room);

        sendMessageToSubscribers(message, sessionByUser);
        sendMessageToRoom(room, message);
    }

    private Message constructSubscribedMessage(User user, Room room) {
        Message result = Message.builder()
                .user(user)
                .room(room)
                .created(LocalDateTime.now())
                .secret(false)
                .text("User " + user.getName() + " subscribed to the room " + room.getName())
                .build();
        return createMessage(result);
    }

    @Override
    public Map<Message, Set<User>> getReceivers() {
        return receiversByMessage;
    }

    @Override
    public void sendMessageToRoom(Room room, Message message) {

        Set<Message> messagesInRoom = messagesByRoom.computeIfAbsent(room, k -> new HashSet<>());

        messagesInRoom.add(message);

        Set<User> receivers;
        if (message.isSecret()) {
            receivers = usersByRoom.get(room)
                    .stream()
                    .filter(user -> user.getRank() >= message.getUser().getRank())
                    .collect(Collectors.toSet());
        } else {
            receivers = new HashSet<>(usersByRoom.get(room));
        }
        receiversByMessage.put(message, receivers);
    }

    @Override
    public void sendMessageToSubscribers(Message message, Map<User, WebSocketSession> sessionByUser) {
        usersByRoom.get(message.getRoom())
                .forEach(user -> {
                    if (message.isSecret() && user.getRank() < message.getUser().getRank()) {
                        return;
                    }

                    WebSocketSession session = sessionByUser.get(user);
                    if (session != null && session.isOpen()) {
                        sendMessageIntoSession(message, session);
                    }
                });
    }

    private void sendMessageIntoSession(Message message, WebSocketSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
