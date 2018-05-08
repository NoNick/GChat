package sample.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.dto.MessageDto;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.model.UserRoomEntranceTime;
import sample.repository.MessageRepository;
import sample.repository.UserRoomEntranceTimeRepository;
import sample.service.MessageConstructor;
import sample.service.RoomService;
import sample.service.UserService;
import sample.service.exceptions.InvalidArgumentException;
import sample.utils.Ranks;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class MessageServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2018, 5, 8, 14, 0);
    private static final LocalDateTime AFTER = LocalDateTime.of(2018, 5, 8, 15, 0);
    private static final Long PUBLIC_SOLDIER_MESSAGE_ID = 1L;
    private static final String ROOM_NAME = "room";
    private static final String SOLDIER_PUBLIC_MESSAGE_TEXT = "Soldier public message";
    private static final String SOLDIER_SECRET_MESSAGE_TEXT = "Soldier secret message";

    private MessageRepository messageRepository;
    private UserRoomEntranceTimeRepository entranceTimeRepository;
    private RoomService roomService;
    private UserService userService;
    private WebSocketSession mockSession;
    private ObjectMapper objectMapper = new ObjectMapper();

    private MessageServiceImpl messageService;

    private UserRoomEntranceTime entranceTimeAfter;

    private Message publicSoldierMessage;
    private Message secretSoldierMessage;
    private Room room;

    private User soldier;
    private User anotherSoldier;
    private User sergeant;

    private List<User> usersInRoom = new ArrayList<>();
    private Map<UUID, WebSocketSession> sessionByUuid = new HashMap<>();

    @Before

    public void setUp() {
        messageRepository = mock(MessageRepository.class);
        entranceTimeRepository = mock(UserRoomEntranceTimeRepository.class);
        roomService = mock(RoomService.class);
        userService = mock(UserService.class);
        mockSession = mock(WebSocketSession.class);

        messageService = new MessageServiceImpl(entranceTimeRepository,
                messageRepository,
                roomService,
                userService, objectMapper);

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
                .textBytes(SOLDIER_PUBLIC_MESSAGE_TEXT.getBytes())
                .secret(false)
                .room(room)
                .user(soldier)
                .build();

        secretSoldierMessage = Message.builder()
                .id(PUBLIC_SOLDIER_MESSAGE_ID)
                .textBytes(SOLDIER_SECRET_MESSAGE_TEXT.getBytes())
                .secret(false)
                .room(room)
                .user(soldier)
                .build();

        entranceTimeAfter = UserRoomEntranceTime.builder()
                .id(2L)
                .time(AFTER)
                .roomName(ROOM_NAME)
                .userUuid(anotherSoldier.getUuid())
                .build();

        sessionByUuid.put(soldier.getUuid(), mockSession);

    }

    @Test
    public void createMessage_validMessage() {
        when(messageRepository.save(publicSoldierMessage)).thenReturn(publicSoldierMessage);
        Message actual = messageService.createMessage(publicSoldierMessage);

        assertThat(actual).isEqualToComparingFieldByField(publicSoldierMessage);
    }

    @Test(expected = InvalidArgumentException.class)
    public void createMessage_nullMessage() {
        messageService.createMessage(null);
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

    @Test(expected = InvalidArgumentException.class)
    public void updateMessage_nullMessage() {
        messageService.updateMessage(null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void updateMessage_invalidMessage() {
        publicSoldierMessage.setTextBytes(null);
        messageService.updateMessage(publicSoldierMessage);
    }

    /*Test sendMessage with
        - valid data:
            * two users in room, one in session
            * two users in room, two in session
        - see message of lower ranks
        - don't see messages of higher rank
        - all encrypted messages
        - no encrypted messages
        - message without user
     */

    //Valid input. Two users with equal rank, both in session.
    @Test
    public void sendMessage_validInputData_equalRankOnly_twoUsersInRoom_bothInSession() throws Exception {
        sessionByUuid.put(anotherSoldier.getUuid(), mockSession);

        usersInRoom.add(soldier);
        usersInRoom.add(anotherSoldier);

        int usersInRoomCount = usersInRoom.size();

        initMockInteractionsForMessageSending(usersInRoom, publicSoldierMessage);

        MessageDto actual = messageService.sendMessage(room, publicSoldierMessage, sessionByUuid);

        assertThat(actual.getText()).isEqualToIgnoringCase(SOLDIER_PUBLIC_MESSAGE_TEXT);

        verifyMockInteractionsForMessageSending(usersInRoomCount, publicSoldierMessage);
    }

    //Valid input. Two users with equal rank, only one in session.
    @Test
    public void sendMessage_validInputData_equalRankOnly_twoUsersInRoom_oneInSession() throws Exception {
        usersInRoom.add(anotherSoldier);
        usersInRoom.add(soldier);

        initMockInteractionsForMessageSending(usersInRoom, publicSoldierMessage);

        MessageDto actual = messageService.sendMessage(room, publicSoldierMessage, sessionByUuid);

        assertThat(actual.getText()).isEqualToIgnoringCase(SOLDIER_PUBLIC_MESSAGE_TEXT);

        verifyMockInteractionsForMessageSending(1, publicSoldierMessage);
    }

    //Valid input. One user in room, one in session.
    @Test
    public void sendMessage_validInputData_oneUserInRoom_oneInSession() throws Exception {

        List<User> users = new ArrayList<>();
        users.add(soldier);

        initMockInteractionsForMessageSending(users, publicSoldierMessage);

        MessageDto actual = messageService.sendMessage(room, publicSoldierMessage, sessionByUuid);

        assertThat(actual.getText()).isEqualToIgnoringCase(SOLDIER_PUBLIC_MESSAGE_TEXT);

        verifyMockInteractionsForMessageSending(1, publicSoldierMessage);
    }

    //Valid input. Encryption
    @Test
    public void sendMessage_validInputData_sendEncryptedMessage() throws Exception {

        initMockInteractionsForMessageSending(usersInRoom, secretSoldierMessage);

        MessageDto actual = messageService.sendMessage(room, secretSoldierMessage, sessionByUuid);

        assertThat(actual.getText()).isNotEqualToIgnoringCase(SOLDIER_PUBLIC_MESSAGE_TEXT);

        verifyMockInteractionsForMessageSending(1, secretSoldierMessage);
    }

    //Room without name
    @Test(expected = InvalidArgumentException.class)
    public void sendMessage_invalidRoom_nullName() throws Exception {
        Room room = this.room;
        room.setName(null);

        messageService.sendMessage(room, secretSoldierMessage, sessionByUuid);
    }

    //Message without text
    @Test(expected = InvalidArgumentException.class)
    public void sendMessage_invalidMessage_nullText() throws Exception {
        Message secretSoldierMessage = this.secretSoldierMessage;
        secretSoldierMessage.setTextBytes(null);
        messageService.sendMessage(room, secretSoldierMessage, sessionByUuid);
    }

    //Null room
    @Test(expected = InvalidArgumentException.class)
    public void sendMessage_nullRoom() throws Exception {
        messageService.sendMessage(null, secretSoldierMessage, sessionByUuid);
    }

    //Null message
    @Test(expected = InvalidArgumentException.class)
    public void sendMessage_nullMessage() throws Exception {
        messageService.sendMessage(room, null, sessionByUuid);
    }

    //Null session map
    @Test(expected = InvalidArgumentException.class)
    public void sendMessage_nullSessionMap() throws Exception {
        messageService.sendMessage(room, publicSoldierMessage, null);
    }

    /*
        What to test:
            - show messages by time:
                * can see only sent after login time
                * can't see before login time
            - show encrypted messages
            - show decrypted messages
            - invalid data:
                * null user
                * null room
                * null map
     */

    //Valid data. Get public messages after user login time
    @Test
    public void showMessagesForUserInRoom_seePublicMessagesSentAfterUserLogin() throws IOException {
        getMessageAfterLoginTime(publicSoldierMessage);
    }


    //Valid data. Get public encrypted after user login time
    @Test
    public void showMessagesForUserInRoom_seeMessagesEncryptedSentAfterUserLogin() throws IOException {
        getMessageAfterLoginTime(secretSoldierMessage);
    }

    //Valid data. Can't get public messages before user login time
    @Test
    public void showMessagesForUserInRoom_cantSeePublicMessagesBeforeLoginTime() throws IOException {
        cantGetMessageBeforeLoginTime(publicSoldierMessage);
    }

    @Test
    public void showMessagesForUserInRoom_cantSeeEncryptedMessagesBeforeLoginTime() throws IOException {
        cantGetMessageBeforeLoginTime(secretSoldierMessage);
    }

    @Test(expected = InvalidArgumentException.class)
    public void showMessagesForUserInRoom_nullUser() {
        messageService.showMessagesForUserInRoom(null, room, sessionByUuid);
    }

    @Test(expected = InvalidArgumentException.class)
    public void showMessagesForUserInRoom_nullRoom() {
        messageService.showMessagesForUserInRoom(soldier, null, sessionByUuid);
    }

    @Test(expected = InvalidArgumentException.class)
    public void showMessagesForUserInRoom_nullMap() {
        messageService.showMessagesForUserInRoom(soldier, room, null);
    }

    @Test
    public void salute_authorized() {
        String actual = messageService.salute("Simon", "E+pl1T31nObs76mdbZORgQ==");
        assertThat(actual).isEqualTo("You are Soldier");
    }

    @Test
    public void salute_unauthorized() {
        String actual = messageService.salute("Her s gory", "lol");
        assertThat(actual).isEqualTo("Unauthorized");
    }

    private void getMessageAfterLoginTime(Message publicSoldierMessage) throws IOException {
        MessageDto expectedDto = MessageConstructor.toDto(publicSoldierMessage);

        HashSet<Message> messagesAfterLogin = new HashSet<>();
        messagesAfterLogin.add(publicSoldierMessage);

        initMockInteractionsForMessageShow(messagesAfterLogin);

        List<MessageDto> actual = messageService.showMessagesForUserInRoom(soldier, room, sessionByUuid);

        assertThat(actual.get(0)).isEqualToComparingFieldByField(expectedDto);

        verifyMockInteractionsForMessageSend();
    }

    private void cantGetMessageBeforeLoginTime(Message publicSoldierMessage) throws IOException {
        HashSet<Message> messagesAfterLogin = new HashSet<>();
        messagesAfterLogin.add(publicSoldierMessage);

        initMockInteractionsForMessageShow(messagesAfterLogin);

        List<MessageDto> actual = messageService.showMessagesForUserInRoom(soldier, room, sessionByUuid);

        assertThat(actual).isEmpty();

        verifyMockInteractionsForMessageSend();
    }

    //Valid data. Can't get encrypted messages before user login time
    private void initMockInteractionsForMessageShow(HashSet<Message> messagesAfterLogin) throws IOException {
        mockWebSocketSession();

        when(roomService.findOrCreateRoom(ROOM_NAME)).thenReturn(room);
        when(messageRepository.findAllBySecret(true)).thenReturn(new HashSet<>());
        when(messageRepository.findAllByRoomName(ROOM_NAME)).thenReturn(messagesAfterLogin);
        when(entranceTimeRepository.findByRoomNameAndUserUuid(ROOM_NAME, soldier.getUuid())).thenReturn(entranceTimeAfter);
    }

    private void verifyMockInteractionsForMessageSend() throws IOException {
        verifyMockSession(1);
        verify(roomService, times(1)).findOrCreateRoom(ROOM_NAME);
        verify(messageRepository, times(1)).findAllBySecret(true);
        verify(messageRepository, times(1)).findAllByRoomName(ROOM_NAME);
        verify(entranceTimeRepository, times(1)).findByRoomNameAndUserUuid(ROOM_NAME, soldier.getUuid());
    }

    private void initMockInteractionsForMessageSending(List<User> userCollection, Message message) throws IOException {
        mockWebSocketSession();
        when(roomService.findOrCreateRoom(ROOM_NAME)).thenReturn(room);
        when(userService.getAllUsersInRoom(room)).thenReturn(userCollection);
        when(messageRepository.save(message)).thenReturn(message);
    }

    private void verifyMockInteractionsForMessageSending(int interactionTimes, Message message) throws IOException {
        verifyMockSession(interactionTimes);
        verify(messageRepository, times(1)).save(message);
        verify(roomService).findOrCreateRoom(ROOM_NAME);
    }

    private void mockWebSocketSession() throws IOException {
        when(mockSession.isOpen()).thenReturn(true);
        doNothing().when(mockSession).sendMessage(any(TextMessage.class));
    }

    private void verifyMockSession(int interactionTimes) throws IOException {
        verify(mockSession, times(interactionTimes)).isOpen();
        verify(mockSession, times(interactionTimes)).sendMessage(any(TextMessage.class));
    }
}
