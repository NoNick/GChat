package sample.unit.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sample.model.User;
import sample.service.UserService;
import sample.service.UserValidation;
import sample.service.impl.UserValidationImpl;
import sample.utils.Ranks;

import java.util.UUID;

import static org.mockito.Mockito.when;

public class UserValidationImplTest {

    private static final int SOLDIER_RANK = Ranks.SOLDIER_RANK;
    private static final String USERNAME = "Username";
    private static final String HASH = "dFP8rE3hVF9izSkcW4J0bQ==";

    private UserService userService;
    private UserValidation userValidation;

    private User user;

    @Before
    public void setUp() {
        userService = Mockito.mock(UserService.class);
        userValidation = new UserValidationImpl(userService);

        user = User.builder()
                .name(USERNAME)
                .rank(SOLDIER_RANK)
                .uuid(UUID.randomUUID())
                .build();
    }

    @Test
    public void getValidUser_validInput() throws Exception {
        when(userService.createUser(user)).thenReturn(user);
        when(userService.getUserByName(USERNAME)).thenReturn(user);

        User validUser = userValidation.getValidUser(USERNAME, HASH);

        Assertions.assertThat(validUser).isEqualToComparingFieldByField(user);
    }

    @Test
    public void getValidUser_nullName() throws Exception {

        User validUser = userValidation.getValidUser(null, HASH);

        Assertions.assertThat(validUser).isNull();
    }

    @Test
    public void getValidUser_nullHash() throws Exception {
        User validUser = userValidation.getValidUser(USERNAME, null);

        Assertions.assertThat(validUser).isNull();
    }

    @Test
    public void getValidUser_invalidHash() throws Exception {
        User validUser = userValidation.getValidUser(USERNAME, "huh");

        Assertions.assertThat(validUser).isNull();
    }
}