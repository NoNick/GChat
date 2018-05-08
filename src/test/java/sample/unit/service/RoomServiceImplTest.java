package sample.unit.service;

import org.junit.Before;
import org.junit.Test;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.repository.RoomRepository;
import sample.repository.UserRepository;
import sample.service.exceptions.InvalidArgumentException;
import sample.service.impl.RoomServiceImpl;
import sample.utils.Ranks;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RoomServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String USERNAME_1 = "Guy1";
    private static final String ROOM_1_NAME = "room1";
    private static final String ROOM_2_NAME = "room2";

    private RoomRepository roomRepository;
    private UserRepository userRepository;

    private RoomServiceImpl roomService;

    private Room room1;
    private User user1;

    private Set<Message> dummyMessageSet1 = new HashSet<>();
    private List<Room> roomList = new ArrayList<>();


    @Before
    public void setUp() {
        roomRepository = mock(RoomRepository.class);
        userRepository = mock(UserRepository.class);

        roomService = new RoomServiceImpl(roomRepository, userRepository);

        dummyMessageSet1.add(
                Message.builder()
                        .textBytes("some textBytes".getBytes())
                        .created(NOW)
                        .secret(false)
                        .build()
        );

        user1 = User.builder()
                .name(USERNAME_1)
                .rank(Ranks.SOLDIER_RANK)
                .userRooms(new HashSet<>())
                .build();

        room1 = Room.builder()
                .name(ROOM_1_NAME)
                .messages(dummyMessageSet1)
                .users(new HashSet<>())
                .build();

        Room room2 = Room.builder()
                .name(ROOM_2_NAME)
                .messages(Collections.emptySet())
                .users(new HashSet<>())
                .build();

        roomList.add(room1);
        roomList.add(room2);
    }


    @Test
    public void getMessagesCountInAllRooms() {
        when(roomRepository.findAll()).thenReturn(roomList);

        Map<String, Integer> actual = roomService.getMessagesCountInAllRooms();

        System.out.println(actual);

        verify(roomRepository).findAll();

        assertThat(actual).containsKey("room1").containsValues(1);
        assertThat(actual).containsKey("room2").containsValues(0);
    }

    @Test
    public void getRoomByName() {
        when(roomRepository.findOne(ROOM_1_NAME)).thenReturn(room1);
        roomService.getRoomByName(ROOM_1_NAME);
    }

    @Test(expected = InvalidArgumentException.class)
    public void getRoomByName_nullName() {
        roomService.getRoomByName(null);
    }

    @Test
    public void findOrCreateRoom_roomDoesntExist() {
        when(roomRepository.exists(ROOM_1_NAME)).thenReturn(false);
        when(roomRepository.save(room1)).thenReturn(room1);

        Room actual = roomService.findOrCreateRoom(ROOM_1_NAME);

        assertThat(actual).isEqualToComparingFieldByField(room1);
    }

    @Test
    public void findOrCreateRoom_roomExist() {
        when(roomRepository.exists(ROOM_1_NAME)).thenReturn(true);
        when(roomRepository.findOne(ROOM_1_NAME)).thenReturn(room1);

        Room actual = roomService.findOrCreateRoom(ROOM_1_NAME);

        assertThat(actual).isEqualToComparingFieldByField(room1);
    }

    @Test
    public void setUserToRoom_validBoth() {
        when(userRepository.findOne(USERNAME_1)).thenReturn(user1);
        when(userRepository.save(user1)).thenReturn(user1);

        when(roomRepository.findOne(ROOM_1_NAME)).thenReturn(room1);
        when(roomRepository.save(room1)).thenReturn(room1);

        roomService.setUserToRoom(user1, room1);

        assertThat(user1.getUserRooms()).contains(room1);
        assertThat(room1.getUsers()).contains(user1);
    }

    @Test(expected = InvalidArgumentException.class)
    public void setUserToRoom_nullUser() {
        roomService.setUserToRoom(null, room1);
    }

    @Test(expected = InvalidArgumentException.class)
    public void setUserToRoom_nullRoom() {
        roomService.setUserToRoom(user1, null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void setUserToRoom_invalidUser_withoutRank() {
        user1.setRank(null);
        roomService.setUserToRoom(user1, room1);
    }

    @Test(expected = InvalidArgumentException.class)
    public void setUserToRoom_invalidRoom_withoutName() {
        room1.setName(null);
        roomService.setUserToRoom(user1, room1);
    }
}