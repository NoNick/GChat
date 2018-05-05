package sample.service;

import sample.model.Room;
import sample.model.User;

import java.util.Set;

public interface UserService {

    User getUserByName(String name);

    User createUser(User user);

    Set<User> getAllUsersInRoom(Room room);
}
