package sample.dao;

import sample.model.Room;

import java.util.Map;

public interface RoomDao {

    Room getRoomByRoomId(String roomId);

    Map<String, Integer> getMessagesCountInAllRooms();
}
