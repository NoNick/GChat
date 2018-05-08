package sample.service;

import sample.model.Room;
import sample.model.User;

import java.util.Map;

public interface RoomService {

    Map<String, Integer> getMessagesCountInAllRooms();

    Room getRoomByName(String roomName);

    Room findOrCreateRoom(String roomName);

    void setUserToRoom(User user, Room room);
}
