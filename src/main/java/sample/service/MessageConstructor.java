package sample.service;

import sample.model.Message;
import sample.model.Room;
import sample.model.User;

public class MessageConstructor {

    public static Message constructMessage(User user, Room room, String text, boolean secret) {
        return Message.builder()
                .text(text)
                .secret(secret)
                .user(user)
                .room(room)
                .build();
    }
}
