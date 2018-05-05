package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import java.util.Map;
import java.util.Set;

public interface MessagingService {
//    Map<Room, Integer> getRoomMessagesNumber();

    Map<Message, Set<User>> getReceivers();

    void report(User user, Room room, String text, boolean secret, Map<User, WebSocketSession> sessionByUser);

    void subscribeUser(Room room, User user, Map<User, WebSocketSession> sessionByUser);

    void addMessageInRoom(Room room, Message message);

    void sendMessageToSubscribers(Message message, Map<User, WebSocketSession> sessionByUser);
}
