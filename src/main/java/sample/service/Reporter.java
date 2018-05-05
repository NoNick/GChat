package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import java.util.Map;

public interface Reporter {

    Message report(User user, Room room, String text, boolean secret, Map<User, WebSocketSession> sessionByUser);

}
