package sample.service.impl;

import org.junit.Before;
import org.junit.Test;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.repository.MessageRepository;
import sample.repository.UserRoomEntranceTimeRepository;
import sample.service.RoomService;
import sample.service.UserService;
import sample.service.exceptions.InvalidArgumentException;
import sample.utils.Ranks;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final long PUBLIC_SOLDIER_MESSAGE_ID = 1L;

    private MessageRepository messageRepository;
    private UserRoomEntranceTimeRepository entranceTimeRepository;
    private RoomService roomService;
    private UserService userService;
    private SessionMessageSender sessionMessageSender;

    private MessageServiceImpl messageService;

    private Message publicSoldierMessage;
    private Room room;

    private User soldier;
    private User anotherSoldier;
    private User sergeant;

    @Before
    public void setUp() {
        messageRepository = mock(MessageRepository.class);
        entranceTimeRepository = mock(UserRoomEntranceTimeRepository.class);
        roomService = mock(RoomService.class);
        userService = mock(UserService.class);
        sessionMessageSender = mock(SessionMessageSender.class);

        messageService = new MessageServiceImpl(entranceTimeRepository,
                sessionMessageSender,
                messageRepository,
                roomService,
                userService);

        room = Room.builder()
                .name("room")
                .messages(new HashSet<>())
                .users(new HashSet<>())
                .build();

        soldier = User.builder()
                .name("Soldier1")
                .rank(Ranks.SOLDIER_RANK)
                .uuid(UUID.randomUUID())
                .userRooms(new HashSet<>())
                .messages(new HashSet<>())
                .build();

        anotherSoldier = User.builder()
                .name("Soldier2")
                .rank(Ranks.SOLDIER_RANK)
                .uuid(UUID.randomUUID())
                .userRooms(new HashSet<>())
                .messages(new HashSet<>())
                .build();

        sergeant = User.builder()
                .name("Sergeant")
                .rank(Ranks.SERGEANT_RANK)
                .uuid(UUID.randomUUID())
                .userRooms(new HashSet<>())
                .messages(new HashSet<>())
                .build();

        publicSoldierMessage = Message.builder()
                .id(PUBLIC_SOLDIER_MESSAGE_ID)
                .created(NOW)
                .secret(false)
                .room(room)
                .user(soldier)
                .build();
    }

    @Test
    public void createMessage_validMessage() {
        when(messageRepository.save(publicSoldierMessage)).thenReturn(publicSoldierMessage);
        Message actual = messageService.createMessage(publicSoldierMessage);

        assertThat(actual).isEqualToComparingFieldByField(publicSoldierMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMessage_nullMessage() {
        messageService.createMessage(publicSoldierMessage);
    }

    @Test(expected = InvalidArgumentException.class)
    public void createMessage_invalidMessage() {
        publicSoldierMessage.setTextBytes(null);
        messageService.createMessage(publicSoldierMessage);
    }

    @Test
    public void updateMessage_validMessage() {
        when(messageRepository.exists(PUBLIC_SOLDIER_MESSAGE_ID)).thenReturn(true);
        when(messageRepository.save(publicSoldierMessage)).thenReturn(publicSoldierMessage);
        Message actual = messageService.updateMessage(publicSoldierMessage);

        assertThat(actual).isEqualToComparingFieldByField(publicSoldierMessage);
    }

    @Test(expected = NoSuchElementException.class)
    public void updateMessage_messageDoesntExistsInDB() {
        when(messageRepository.exists(PUBLIC_SOLDIER_MESSAGE_ID)).thenReturn(false);
        messageService.updateMessage(publicSoldierMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMessage_nullMessage() {
        messageService.updateMessage(null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void updateMessage_invalidMessage() {
        publicSoldierMessage.setTextBytes(null);
        messageService.updateMessage(publicSoldierMessage);
    }

    /*Test sendMessage with
        - valid data
        - no users in room
        - all encrypted messages
        - no encrypted messages
        - message without user
     */
    @Test
    public void sendMessage() {

    }

    @Test
    public void showMessagesForUserInRoom() {
    }

    @Test
    public void salute() {
    }
}
