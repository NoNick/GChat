package sample.service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sample.dto.WSMessage;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.service.*;
import sample.utils.Ranks;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WSHandler extends TextWebSocketHandler {

    private final Map<UUID, WebSocketSession> sessionByUUID = new ConcurrentHashMap<>();

    private final SubscriptionService subscriptionService;
    private final RoomService roomService;
    private final UserService userService;
    private final UserValidation userValidation;
    private final MessageService messageService;

    @Autowired
    public WSHandler(SubscriptionService subscriptionService,
                     RoomService roomService,
                     UserService userService,
                     UserValidation userValidation,
                     MessageService messageService) {
        this.subscriptionService = subscriptionService;
        this.roomService = roomService;
        this.userService = userService;
        this.userValidation = userValidation;
        this.messageService = messageService;
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

        User user = userValidation.getValidUser(name, hash);
        if (user == null) {
            session.sendMessage(new TextMessage("Unauthorized"));
            return;
        }

        sessionByUUID.put(user.getUuid(), session);
        Room room = roomService.findOrCreateRoom(roomName);

        switch (wsMessage.getAction()) {
            case "salute":
                echo(session, user);
                break;
            case "report":
                if (!userService.containsUserInRoom(user, room)) {
                    subscriptionService.subscribeUser(room, user, sessionByUUID);
                }
                report(messageText, secret, user, room);
                break;
            case "subscribe":
                subscriptionService.subscribeUser(room, user, sessionByUUID);
                break;
            case "look":
                messageService.showMessagesForUserInRoom(user, room, sessionByUUID);
                break;
        }
    }

    private void report(String messageText, Boolean secret, User user, Room room) {
        Message msg = MessageConstructor.constructMessage(user, room, messageText, secret);
        messageService.sendMessage(room, msg, sessionByUUID);
    }

    private void echo(WebSocketSession session, User user) throws IOException {
        session.sendMessage(new TextMessage("Your rank is " + Ranks.getRankName(user.getRank())));
    }


}
