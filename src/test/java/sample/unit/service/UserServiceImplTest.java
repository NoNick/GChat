package sample.unit.service;

import org.junit.Before;
import org.junit.Test;
import sample.model.Room;
import sample.model.User;
import sample.repository.UserRepository;
import sample.service.exceptions.InvalidArgumentException;
import sample.service.impl.UserServiceImpl;
import sample.utils.Ranks;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {

    private static final String USERNAME = "Username";
    private UserRepository userRepository;
    private UserServiceImpl userService;

    private User user;
    private Room room;


    @Before
    public void setUp() throws Exception {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);

        room = Room.builder()
                .name("room")
                .build();

        user = User.builder()
                .name(USERNAME)
                .rank(Ranks.SOLDIER_RANK)
                .uuid(UUID.randomUUID())
                .userRooms(new HashSet<>())
                .messages(new HashSet<>())
                .build();

    }

    @Test
    public void getUserByName() {
        when(userRepository.findOne(USERNAME)).thenReturn(user);
        User actual = userService.getUserByName(USERNAME);

        assertThat(actual).isEqualToComparingFieldByField(user);
    }

    @Test(expected = InvalidArgumentException.class)
    public void getUserByName_nullName() {
        userService.getUserByName(null);
    }

    @Test
    public void createUser_validUser() {
        when(userRepository.save(user)).thenReturn(user);
        User actual = userService.createUser(this.user);
        assertThat(actual).isEqualToComparingFieldByField(user);
    }

    @Test(expected = InvalidArgumentException.class)
    public void createUser_invalidUser_nullName() {
        user.setName(null);
        userService.createUser(user);
    }

    @Test(expected = InvalidArgumentException.class)
    public void createUser_nullUser() {
        userService.createUser(null);
    }

    @Test
    public void getAllUsersInRoom() {
        when(userRepository.findAllByUserRoomsContaining(room)).thenReturn(Collections.singletonList(user));
        List<User> actual = userService.getAllUsersInRoom(room);
        assertThat(actual).contains(user);
    }

    @Test
    public void containsUserInRoom_contains() {
        Set<Room> rooms = new HashSet<>();
        rooms.add(room);

        user.setUserRooms(rooms);
        when(userRepository.findOne(USERNAME)).thenReturn(user);

        boolean actual = userService.containsUserInRoom(user, room);

        assertThat(actual).isTrue();
    }

    @Test
    public void containsUserInRoom_notContains() {
        Set<Room> rooms = new HashSet<>();

        user.setUserRooms(rooms);
        when(userRepository.findOne(USERNAME)).thenReturn(user);

        boolean actual = userService.containsUserInRoom(user, room);

        assertThat(actual).isFalse();
    }

    @Test(expected = InvalidArgumentException.class)
    public void containsUserInRoom_nullUser() {
        userService.containsUserInRoom(null, room);
    }

    @Test(expected = InvalidArgumentException.class)
    public void containsUserInRoom_nullRoom() {
        userService.containsUserInRoom(user, null);
    }
}