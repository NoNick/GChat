package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.model.Room;
import sample.model.User;

import java.util.Map;

public interface SubscriptionService {

    void subscribeUser(Room room, User user, Map<User, WebSocketSession> sessionByUser);
}
