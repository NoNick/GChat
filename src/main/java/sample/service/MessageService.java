package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import java.util.Map;
import java.util.Set;

public interface MessageService {

    Message createMessage(Message message);

    Map<Message, Set<User>> getReceivers();

    Map<Room, Set<User>> getUsersByRoom();

    void sendMessageToRoom(Room room, Message message);

    void sendMessageToSubscribers(Message message, Map<User, WebSocketSession> sessionByUser);


}
