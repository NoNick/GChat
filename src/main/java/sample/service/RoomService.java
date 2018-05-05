package sample.service;

import sample.model.Room;

import java.util.List;
import java.util.Map;

public interface RoomService {

    Map<String, Integer> getMessagesCountInAllRooms();

    List<Room> getAllRooms();

    Room getRoomByName(String roomName);

    Room createRoom(Room room);

    Room updatRoom(Room room);

    void deleteRoom(String name);

    boolean exists(Room room);
}
