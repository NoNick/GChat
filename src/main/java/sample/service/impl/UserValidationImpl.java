package sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sample.model.User;
import sample.service.UserService;
import sample.service.UserValidation;
import sample.utils.Ranks;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserValidationImpl implements UserValidation {

    private final UserService userService;

    @Autowired
    public UserValidationImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public User getValidUser(String name, String hash) {
        User user = userService.getUserByName(name);

        Optional<Integer> maybeRank = Ranks.getRank(name, hash);
        if (!maybeRank.isPresent()) {
            return null;
        }
        if (user == null) {
            user = User.builder()
                    .uuid(UUID.randomUUID())
                    .name(name)
                    .rank(maybeRank.get())
                    .build();
            userService.createUser(user);
        }
        return user;
    }
}
