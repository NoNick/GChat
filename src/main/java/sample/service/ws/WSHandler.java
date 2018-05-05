package sample.service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import sample.Ranks;
import sample.dto.WSMessage;
import sample.model.Room;
import sample.model.User;
import sample.service.MessagingService;
import sample.service.RoomService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class WSHandler implements WebSocketHandler {

    private final Map<User, WebSocketSession> sessionByUser = new HashMap<>();

    @PersistenceContext
    EntityManager entityManager;

    private final MessagingService messagingService;
    private final RoomService roomService;

    @Autowired
    public WSHandler(MessagingService messagingService, RoomService roomService) {
        this.messagingService = messagingService;
        this.roomService = roomService;
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
        Room room = null;
        if (roomName != null) {
            room = entityManager.find(Room.class, roomName);
            if (room == null) {
                room = new Room();
                room.setName(roomName);
                entityManager.persist(room);
            }
        }

        switch (wsMessage.getAction()) {
            case "salute":
                session.sendMessage(new TextMessage("Your rank is " + Ranks.getRankName(user.getRank())));
                break;
            case "report":
                messagingService.report(user, room, messageText, secret, sessionByUser);
                break;
            case "subscribe":
                messagingService.subscribeUser(room, user, sessionByUser);
                break;
        }
    }

    private User getUser(String name, String hash) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        User user = entityManager.find(User.class, name);
        Optional<Integer> maybeRank = Ranks.getRank(name, hash);
        if (!maybeRank.isPresent()) {
            return null;
        }
        if (user == null) {
            user = new User();
            user.setName(name);
            user.setRank(maybeRank.get());
            entityManager.persist(user);
        }
        return user;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {}

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {}

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {}

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
