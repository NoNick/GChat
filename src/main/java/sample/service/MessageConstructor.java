package sample.service;

import sample.dto.MessageDto;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.utils.crypto.Cryptography;

public class MessageConstructor {

    public static Message constructMessage(User user, Room room, String text, boolean secret) {
        return Message.builder()
                .textBytes(text.getBytes())
                .secret(secret)
                .user(user)
                .room(room)
                .build();
    }

    public static MessageDto toDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .created(message.getCreated())
                .secret(message.isSecret())
                .text(new String(message.getTextBytes()))
                .build();
    }

    public static Message getMessageWithEncryptedText(Message message) {
        return Message.builder()
                .id(message.getId())
                .secret(message.isSecret())
                .created(message.getCreated())
                .room(message.getRoom())
                .user(message.getUser())
                .textBytes(Cryptography.encryptMessageWithRank(message, message.getUser().getRank()))
                .build();
    }

}
