package sample.service;

import sample.model.Room;

import java.util.Map;

public interface RoomService {

    Map<String, Integer> getMessagesCountInAllRooms();

    Room getRoomByName(String roomName);

    Room findOrCreateRoom(String roomName);

}
