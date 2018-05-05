package sample.service;

import sample.model.Message;
import sample.model.Room;
import sample.model.User;

import java.util.List;

public interface MessageService {

    List<Message> getAllMessages();

    List<Message> getMessagesByUser(User user);

    Message getMessageById(Long id);

    Message createMessage(Message message);

    Message updateMessage(Message message);

    void deleteMessageById(Long id);

    List<Message> getAllMessagesInRoom(Room room);

    Message sendMessage(Message message, Room room);
}
