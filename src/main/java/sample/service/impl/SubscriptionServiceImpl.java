package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.service.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final MessageService messageService;
    private final UserService userService;
    private final RoomService roomService;

    @Autowired
    public SubscriptionServiceImpl(MessageService messageService, UserService userService, RoomService roomService) {
        this.messageService = messageService;
        this.userService = userService;
        this.roomService = roomService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void subscribeUser(Room room, User user, Map<UUID, WebSocketSession> sessionByUser) {
        if (!isUserSubscribed(room, user, sessionByUser)) {

            roomService.setUserToRoom(user, room);

            String subscriptionMessage = "User " + user.getName() + " subscribed to the room " + room.getName();
            Message message = MessageConstructor.constructMessage(user, room, subscriptionMessage, false);

            messageService.sendMessageToSubscribers(message, sessionByUser);
        }

    }

    private boolean isUserSubscribed(Room room, User user, Map<UUID, WebSocketSession> sessionByUser) {
        if (userService.containsUserInRoom(user, room)) {
            try {
                sessionByUser.get(user.getUuid()).sendMessage(new TextMessage("You already subscribed to room!"));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
