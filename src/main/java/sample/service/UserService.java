package sample.service;

import sample.model.Room;
import sample.model.User;

import java.util.List;

public interface UserService {

    User getUserByName(String name);

    User createUser(User user);

    List<User> getAllUsersInRoom(Room room);

    boolean containsUserInRoom(User user, Room room);
}
