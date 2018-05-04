package sample.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessagingServiceImpl implements MessagingService {
    @PersistenceContext
    private EntityManager entityManager;


    private final Map<Room, Set<User>> usersByRoom = new HashMap<>();
    private final Map<Room, Set<Message>> messagesByRoom = new HashMap<>();
    private final Map<Message, Set<User>> receiversByMessage = new HashMap<>();

    @Override
    public Map<Room, Integer> getRoomMessagesNumber() {
        Map<Room, Integer> numberByRoom = new HashMap<>();

        entityManager.createQuery("from Room", Room.class).getResultList().forEach(room -> {
            numberByRoom.put(room, room.getMessages().size());
        });

        return numberByRoom;
    }

    @Override
    public Map<Message, Set<User>> getReceivers() {
        return receiversByMessage;
    }

    @Override
    public void report(User user, Room room, String text, boolean secret, Map<User, WebSocketSession> sessionByUser) {
        synchronized (messagesByRoom) {
            subscribeUser(room, user, sessionByUser);

            Message msg = constructMessage(user, room, text, secret);
            addMessageInRoom(room, msg);
            sendMessageToSubscribers(msg, sessionByUser);
        }
    }

    @Override
    public void subscribeUser(Room room, User user, Map<User, WebSocketSession> sessionByUser) {
        synchronized (usersByRoom) {

            Set<User> users = usersByRoom.get(room);
            if (users == null) {
                users = new HashSet<>();
                usersByRoom.put(room, users);
            }
            if (usersByRoom.get(room).contains(user)) {
                return;
            }
            users.add(user);

            Message message = constructSubscribedMessage(user, room);
            synchronized (messagesByRoom) {
                sendMessageToSubscribers(message, sessionByUser);
                addMessageInRoom(room, message);
            }
        }
    }

    @Override
    public void addMessageInRoom(Room room, Message message) {
        synchronized (usersByRoom) {
            Set<Message> messagesInRoom = messagesByRoom.get(room);
            if (messagesInRoom == null) {
                messagesInRoom = new HashSet<>();
                messagesByRoom.put(room, messagesInRoom);
            }
            messagesInRoom.add(message);

            Set<User> receivers;
            if (message.isSecret()) {
                receivers = usersByRoom.get(room).stream()
                        .filter(user -> user.getRank() >= message.getUser().getRank()).collect(Collectors.toSet());
            } else {
                receivers = new HashSet<>(usersByRoom.get(room));
            }
            receiversByMessage.put(message, receivers);
        }
    }

    @Override
    public void sendMessageToSubscribers(Message message, Map<User, WebSocketSession> sessionByUser) {
        synchronized (usersByRoom) {
            usersByRoom.get(message.getRoom()).forEach(user -> {
                if (message.isSecret() && user.getRank() < message.getUser().getRank()) {
                    return;
                }

                WebSocketSession session = sessionByUser.get(user);
                if (session != null && session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message.toJSON().toJSONString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private Message constructMessage(User user, Room room, String text, boolean secret) {
        Message result = new Message();
        result.setText(text);
        result.setSecret(secret);
        result.setEpoch(System.currentTimeMillis());
        result.setUser(user);
        result.setRoom(room);
        result.setEpoch(System.currentTimeMillis());
        entityManager.persist(result);
        entityManager.flush(); // set id for the massage
        return result;
    }

    private Message constructSubscribedMessage(User user, Room room) {
        Message result = new Message();
        result.setUser(user);
        result.setRoom(room);
        result.setEpoch(System.currentTimeMillis() / 1000);
        result.setSecret(false);
        result.setText("User " + user.getName() + " subscribed to the room " + room.getName());
        entityManager.persist(result);
        entityManager.flush(); // set id for the massage
        return result;
    }
}
