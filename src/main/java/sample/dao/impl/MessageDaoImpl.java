package sample.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.dao.MessageDao;
import sample.dao.RoomDao;
import sample.dao.UserDao;
import sample.model.Message;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MessageDaoImpl implements MessageDao {

    @PersistenceContext
    private final EntityManager entityManager;

    private final UserDao userDao;
    private final RoomDao roomDao;

    @Autowired
    public MessageDaoImpl(EntityManager entityManager, UserDao userDao, RoomDao roomDao) {
        this.entityManager = entityManager;
        this.userDao = userDao;
        this.roomDao = roomDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessagesInRoomByRoomId(String roomName) {
        return entityManager.createQuery("SELECT m FROM messages m WHERE m.roomName = :roomName", Message.class)
                .setParameter("roomName", roomName)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessagesOfUserByUserName(String username) {
        return entityManager.createQuery("SELECT m FROM messages m WHERE m.userName = :username", Message.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Message getMessageById(Long messageId) {
        return entityManager.find(Message.class, messageId);
    }

    @Override
    @Transactional
    public Message createMessage(String userName, String roomName, String text, boolean isSecret) {
        Message message = Message.builder()
                .created(LocalDateTime.now())
                .text(text)
                .roomName(roomName)
                .userName(userName)
                .secret(isSecret)
                .user(userDao.getUserByUsername(userName))
                .room(roomDao.getRoomByRoomId(roomName))
                .build();
        entityManager.persist(message);
        entityManager.flush();
        return message;
    }

    @Override
    public Message subscriptionMessage(String userName, String roomName) {
        return null;
    }
}
