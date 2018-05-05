package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.model.Room;
import sample.repository.RoomRepository;
import sample.service.RoomService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getMessagesCountInAllRooms() {
        return roomRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Room::getName,
                        room -> room.getMessages().size())
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Room getRoomByName(String roomName) {
        return roomRepository.findOne(roomName);
    }

    @Override
    @Transactional
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public Room updatRoom(Room room) {
        if (roomRepository.exists(room.getName())) {
            return roomRepository.save(room);
        } else return null;
    }

    @Override
    @Transactional
    public void deleteRoom(String name) {
        roomRepository.delete(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Room room) {
        return room != null && roomRepository.exists(room.getName());
    }
}
