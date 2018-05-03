package sample.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.*;
import sample.Ranks;
import sample.model.Room;
import sample.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
@Transactional
public class WSHandler implements WebSocketHandler {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    MessagingService messagingService;

    private final Map<User, WebSocketSession> sessionByUser = new HashMap<>();

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        JSONObject messageJSON = (JSONObject) new JSONParser().parse(message.getPayload().toString());
        String name = (String) messageJSON.get("name");
        String hash = (String) messageJSON.get("hash");
        String roomName = (String) messageJSON.get("room");
        String messageText = (String) messageJSON.get("message");
        Boolean secret = (Boolean) messageJSON.get("secret");
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

        switch (messageJSON.get("action").toString()) {
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
