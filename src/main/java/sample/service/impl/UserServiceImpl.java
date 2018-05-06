package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sample.model.Room;
import sample.model.User;
import sample.repository.RoomRepository;
import sample.repository.UserRepository;
import sample.service.UserService;
import sample.utils.SimpleValidator;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoomRepository roomRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByName(String name) {
        return userRepository.findOne(name);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<User> getAllUsersInRoom(Room room) {
        return userRepository.findAllByUserRoomsContaining(room);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean containsUserInRoom(User user, Room room) {
        SimpleValidator.validateObject(user, "User must not be null");
        SimpleValidator.validateObject(room, "Room must not be null");
        User userByName = getUserByName(user.getName());
        return userByName.getUserRooms().contains(room) && userByName.getUserRooms() != null;
    }

}
