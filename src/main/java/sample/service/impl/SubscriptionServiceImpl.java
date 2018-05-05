package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.service.MessageService;
import sample.service.SubscriptionService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final MessageService messageService;

    @Autowired
    public SubscriptionServiceImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public void subscribeUser(Room room, User user, Map<User, WebSocketSession> sessionByUser) {

        Map<Room, Set<User>> usersByRoom = messageService.getUsersByRoom();

        Set<User> users = usersByRoom.computeIfAbsent(room, k -> new HashSet<>());

        if (!usersByRoom.get(room).contains(user)) {
            users.add(user);

            Message message = constructSubscribedMessage(user, room);

            messageService.sendMessageToSubscribers(message, sessionByUser);
            messageService.sendMessageToRoom(room, message);
        }
    }

    private Message constructSubscribedMessage(User user, Room room) {
        Message result = Message.builder()
                .user(user)
                .room(room)
                .created(LocalDateTime.now())
                .secret(false)
                .text("User " + user.getName() + " subscribed to the room " + room.getName())
                .build();
        return messageService.createMessage(result);
    }
}
