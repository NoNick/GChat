package sample.service.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.repository.MessageRepository;
import sample.service.MessageService;
import sample.service.RoomService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final RoomService roomService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, RoomService roomService) {
        this.messageRepository = messageRepository;
        this.roomService = roomService;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesByUser(User user) {
        validateObject(user, "User must not be null");
        return messageRepository.findAllByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Message getMessageById(Long id) {
        return messageRepository.findOne(id);
    }

    @Override
    @Transactional
    public Message createMessage(Message message) {
        validateObject(message, "Message must not be null");
        message.setCreated(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public Message updateMessage(Message message) {
        validateObject(message, "Message must not be null");
        if (!messageRepository.exists(message.getId())) {
            throw new ObjectNotFoundException(message.getId(), "Message");
        }
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteMessageById(Long id) {
        if (!messageRepository.exists(id)) {
            throw new ObjectNotFoundException(id, "Message");
        }
        messageRepository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessagesInRoom(Room room) {
        validateObject(room, "Room must not be null");
        return messageRepository.findAllByRoom(room);
    }

    @Override
    @Transactional
    public Message sendMessage(Message message, Room room) {
        validateObject(message, "Message must not be null");
        validateObject(room, "Room must not be null");
        if (!roomService.exists(room)) {
            roomService.createRoom(room);
        }
        message.setRoom(room);
        room.getMessages().add(message);

        return createMessage(message);
    }

    private void validateObject(Object object, String msg) {
        if (object == null) {
            throw new IllegalArgumentException(msg);
        }
    }
}
