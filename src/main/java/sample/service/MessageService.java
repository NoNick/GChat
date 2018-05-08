package sample.service;

import org.springframework.web.socket.WebSocketSession;
import sample.dto.MessageDto;
import sample.dto.Receiver;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MessageService {

    Message createMessage(Message message);

    Message updateMessage(Message message);

    List<Receiver> getReceivers();

    MessageDto sendMessage(Room room, Message message, Map<UUID, WebSocketSession> sessionByUUID);

    List<MessageDto> showMessagesForUserInRoom(User user, Room room, Map<UUID, WebSocketSession> sessionByUUID);

    String salute(String name, String hash);
}
