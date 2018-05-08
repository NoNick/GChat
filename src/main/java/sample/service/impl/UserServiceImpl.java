package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.model.Room;
import sample.model.User;
import sample.repository.UserRepository;
import sample.service.UserService;
import sample.utils.validation.GCValidator;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByName(String name) {
        GCValidator.validateObject(name);
        return userRepository.findOne(name);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        GCValidator.validateObject(user);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<User> getAllUsersInRoom(Room room) {
        GCValidator.validateObject(room);
        return userRepository.findAllByUserRoomsContaining(room);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean containsUserInRoom(User user, Room room) {
        GCValidator.validateObject(user);
        GCValidator.validateObject(room);
        User userByName = getUserByName(user.getName());
        return userByName.getUserRooms().contains(room) && userByName.getUserRooms() != null;
    }

}
