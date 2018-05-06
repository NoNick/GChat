package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.dto.Receiver;
import sample.model.Message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MessageService {

    Message createMessage(Message message);

    List<Receiver> getReceivers();

    void sendMessageToSubscribers(Message message, Map<UUID, WebSocketSession> sessionByUser);


}
