package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MessageService {

    List<Message> getAllMessages();

    List<Message> getMessagesByUser(User user);

    Message createMessage(Message message);

    List<Message> getAllMessagesInRoom(Room room);

    void subscribeUser(Room room, User user, Map<User, WebSocketSession> sessionByUser);

    Map<Message, Set<User>> getReceivers();

    void sendMessageToRoom(Room room, Message message);

    void sendMessageToSubscribers(Message message, Map<User, WebSocketSession> sessionByUser);


}
