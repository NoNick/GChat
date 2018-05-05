package sample.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sample.dao.RoomDao;
import sample.model.Room;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;

@Repository
public class RoomDaoImpl implements RoomDao {

    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public RoomDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Room getRoomByRoomId(String roomId) {
        return entityManager.find(Room.class, roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getMessagesCountInAllRooms() {
        Map<String, Integer> countByRoomName = new HashMap<>();
        entityManager.createQuery("SELECT room FROM rooms room", Room.class)
                .getResultList()
                .forEach(room -> countByRoomName.put(room.getName(), room.getMessages().size()));
        return countByRoomName;
    }
}
