package sample.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sample.model.User;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class SessionMessageSender {

    private final ObjectMapper objectMapper;

    @Autowired
    public SessionMessageSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Consumer<User> sendObjectToSession(Object object, Map<UUID, WebSocketSession> sessionByUser) {
        return user -> {
            WebSocketSession session = sessionByUser.get(user.getUuid());
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(object)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
