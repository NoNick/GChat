package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.model.User;
import sample.repository.UserRepository;
import sample.service.UserService;

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
        return userRepository.findOne(name);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

}
