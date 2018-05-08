package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.model.Room;
import sample.model.User;

import java.util.Map;
import java.util.UUID;

public interface SubscriptionService {

    boolean subscribeUser(Room room, User user, Map<UUID, WebSocketSession> sessionByUser);
}
