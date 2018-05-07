package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.model.Room;
import sample.model.User;
import sample.repository.RoomRepository;
import sample.repository.UserRepository;
import sample.service.RoomService;
import sample.utils.validation.GCValidator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getMessagesCountInAllRooms() {
        List<Room> all = roomRepository.findAll();
        return all
                .stream()
                .collect(Collectors.toMap(
                        Room::getName,
                        room -> room.getMessages().size())
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Room getRoomByName(String roomName) {
        GCValidator.validateObject(roomName);
        return roomRepository.findOne(roomName);
    }

    @Override
    @Transactional
    public Room findOrCreateRoom(String roomName) {
        GCValidator.validateObject(roomName);

        if (roomRepository.exists(roomName)) {
            return getRoomByName(roomName);
        }

        Room room = Room.builder().name(roomName).build();
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public void setUserToRoom(User user, Room room) {
        GCValidator.validateObject(user);
        GCValidator.validateObject(room);

        Room roomFromDB = roomRepository.findOne(room.getName());
        User userFromDB = userRepository.findOne(user.getName());

        roomFromDB.getUsers().add(userFromDB);
        userFromDB.getUserRooms().add(roomFromDB);

        roomRepository.save(roomFromDB);
        userRepository.save(userFromDB);

    }

}
