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
import sample.model.UserRoomEntranceTime;
import sample.repository.UserRoomEntranceTimeRepository;
import sample.service.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final MessageService messageService;
    private final UserService userService;
    private final RoomService roomService;
    private final UserRoomEntranceTimeRepository entranceTimeRepository;

    @Autowired
    public SubscriptionServiceImpl(MessageService messageService,
                                   UserService userService,
                                   RoomService roomService,
                                   UserRoomEntranceTimeRepository entranceTimeRepository) {
        this.messageService = messageService;
        this.userService = userService;
        this.roomService = roomService;
        this.entranceTimeRepository = entranceTimeRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean subscribeUser(Room room, User user, Map<UUID, WebSocketSession> sessionByUser) {

        if (!isUserSubscribed(room, user, sessionByUser)) {
            roomService.setUserToRoom(user, room);

            String subscriptionMessage = "User " + user.getName() + " subscribed to the room " + room.getName();
            Message message = MessageConstructor.constructMessage(user, room, subscriptionMessage, false);

            saveUserEntranceTime(room, user);

            messageService.sendMessage(room, message, sessionByUser);
            return true;
        } else return false;
    }

    private void saveUserEntranceTime(Room room, User user) {
        entranceTimeRepository.save(UserRoomEntranceTime.builder()
                .roomName(room.getName())
                .userUuid(user.getUuid())
                .time(LocalDateTime.now())
                .build());
    }

    private boolean isUserSubscribed(Room room, User user, Map<UUID, WebSocketSession> sessionByUser) {
        boolean b = userService.containsUserInRoom(user, room);
        if (b) {
            try {
                WebSocketSession session = sessionByUser.get(user.getUuid());
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage("You already subscribed to room!"));
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
