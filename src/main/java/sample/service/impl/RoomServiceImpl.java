package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.model.Room;
import sample.repository.RoomRepository;
import sample.service.RoomService;
import sample.utils.SimpleValidator;

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
    public Room getRoomByName(String roomName) {
        SimpleValidator.validateObject(roomName, "Name must not be null");
        return roomRepository.findOne(roomName);
    }

    @Override
    @Transactional
    public Room findOrCreateRoom(String roomName) {
        SimpleValidator.validateObject(roomName, "Room name must not be null");

        if (roomRepository.exists(roomName)) {
            return getRoomByName(roomName);
        }

        Room room = Room.builder().name(roomName).build();
        return roomRepository.save(room);
    }

}
