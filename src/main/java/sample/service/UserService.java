package sample.service;

import sample.model.User;

public interface UserService {

    User getUserByName(String name);

    User createUser(User user);
}
