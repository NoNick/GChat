package sample.service.impl;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.dto.MessageDto;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.model.UserRoomEntranceTime;
import sample.repository.UserRoomEntranceTimeRepository;
import sample.service.MessageService;
import sample.service.RoomService;
import sample.service.SubscriptionService;
import sample.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SubscriptionServiceImplTest {

    private static final String USERNAME = "Username";
    private UserRoomEntranceTimeRepository entranceTimeRepository;
    private MessageService messageService;
    private WebSocketSession mockSession;
    private UserService userService;
    private RoomService roomService;

    private SubscriptionService subscriptionService;

    private Room room;
    private User user;
    private Map<UUID, WebSocketSession> sessionByUuid = new HashMap<>();

    @Before
    public void setUp() {
        entranceTimeRepository = Mockito.mock(UserRoomEntranceTimeRepository.class);
        messageService = Mockito.mock(MessageService.class);
        userService = Mockito.mock(UserService.class);
        roomService = Mockito.mock(RoomService.class);
        mockSession = mock(WebSocketSession.class);

        subscriptionService = new SubscriptionServiceImpl(messageService, userService, roomService, entranceTimeRepository);

        user = User.builder()
                .messages(new HashSet<>())
                .userRooms(new HashSet<>())
                .uuid(UUID.randomUUID())
                .rank(1)
                .name(USERNAME)
                .build();

        room = Room.builder()
                .name("room")
                .users(new HashSet<>())
                .messages(new HashSet<>())
                .build();
        sessionByUuid.put(user.getUuid(), mockSession);
    }

    @Test
    public void subscribeUser_subscribe() throws Exception {
        initMocks(false);

        boolean actual = subscriptionService.subscribeUser(room, user, sessionByUuid);

        Assertions.assertThat(actual).isTrue();
        verify(mockSession, never()).isOpen();
        verify(mockSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    public void subscribeUser_userAlreadySubscribed() throws Exception {
        initMocks(true);

        boolean actual = subscriptionService.subscribeUser(room, user, sessionByUuid);

        Assertions.assertThat(actual).isFalse();
        verify(mockSession, times(1)).isOpen();
        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }

    private void initMocks(boolean b) throws IOException {
        when(mockSession.isOpen()).thenReturn(true);
        doNothing().when(mockSession).sendMessage(any(TextMessage.class));
        MessageDto messageDto = MessageDto.builder().text("Created").build();
        when(messageService.sendMessage(eq(room), any(Message.class), eq(sessionByUuid))).thenReturn(messageDto);
        doNothing().when(roomService).setUserToRoom(user, room);
        when(entranceTimeRepository.save(any(UserRoomEntranceTime.class))).thenReturn(null);
        when(userService.containsUserInRoom(user, room)).thenReturn(b);
    }
}