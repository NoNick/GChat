package sample.dao;

import sample.model.User;

import java.util.List;

public interface UserDao {

    List<User> getAllUsers();

    User getUserByUsername(String username);
}
