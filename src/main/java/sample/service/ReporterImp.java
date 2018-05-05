package sample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ReporterImp implements Reporter {

    private final MessageService messageService;

    @Autowired
    public ReporterImp(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void report(User user, Room room, String text, boolean secret, Map<User, WebSocketSession> sessionByUser) {
        messageService.subscribeUser(room, user, sessionByUser);

        Message msg = constructMessage(user, room, text, secret);
        messageService.sendMessageToRoom(room, msg);
        messageService.sendMessageToSubscribers(msg, sessionByUser);
    }

    private Message constructMessage(User user, Room room, String text, boolean secret) {
        Message result = Message.builder()
                .text(text)
                .secret(secret)
                .user(user)
                .room(room)
                .created(LocalDateTime.now())
                .build();

        return messageService.createMessage(result);
    }
}
