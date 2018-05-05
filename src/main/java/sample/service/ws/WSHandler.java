package sample.service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sample.dto.WSMessage;
import sample.model.Room;
import sample.model.User;
import sample.service.MessageService;
import sample.service.Reporter;
import sample.service.RoomService;
import sample.service.UserService;
import sample.utils.Ranks;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class WSHandler extends TextWebSocketHandler {

    private final Map<User, WebSocketSession> sessionByUser = new HashMap<>();

    private final Reporter reporter;
    private final MessageService messageService;
    private final RoomService roomService;
    private final UserService userService;

    @Autowired
    public WSHandler(Reporter reporter, MessageService messageService, RoomService roomService, UserService userService) {
        this.reporter = reporter;
        this.messageService = messageService;
        this.roomService = roomService;
        this.userService = userService;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        WSMessage wsMessage = objectMapper.readValue(message.getPayload().toString(), WSMessage.class);

        String name = wsMessage.getName();
        String hash = wsMessage.getHash();
        String roomName = wsMessage.getRoom();
        String messageText = wsMessage.getMessage();
        Boolean secret = wsMessage.isSecret();

        User user = getUser(name, hash);
        if (user == null) {
            session.sendMessage(new TextMessage("Unauthorized"));
            return;
        }

        sessionByUser.put(user, session);
        Room room = roomService.findOrCreateRoom(roomName);

        switch (wsMessage.getAction()) {
            case "salute":
                session.sendMessage(new TextMessage("Your rank is " + Ranks.getRankName(user.getRank())));
                break;
            case "report":
                reporter.report(user, room, messageText, secret, sessionByUser);
                break;
            case "subscribe":
                messageService.subscribeUser(room, user, sessionByUser);
                break;
        }
    }

    private User getUser(String name, String hash) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        User user = userService.getUserByName(name);

        Optional<Integer> maybeRank = Ranks.getRank(name, hash);
        if (!maybeRank.isPresent()) {
            return null;
        }
        if (user == null) {
            user = User.builder()
                    .name(name)
                    .rank(maybeRank.get())
                    .build();
            userService.createUser(user);
        }
        return user;
    }
}
