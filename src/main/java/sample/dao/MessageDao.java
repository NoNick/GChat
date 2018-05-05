package sample.dao;

import sample.model.Message;

import java.util.List;

public interface MessageDao {

    List<Message> getAllMessagesInRoomByRoomId(String roomName);

    List<Message> getAllMessagesOfUserByUserName(String userName);

    Message getMessageById(Long messageId);

    Message createMessage(String userName, String roomName, String text, boolean isSecret);

    Message subscriptionMessage(String userName, String roomName);

}
