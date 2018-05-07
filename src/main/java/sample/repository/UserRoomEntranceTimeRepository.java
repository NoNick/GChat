package sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sample.model.UserRoomEntranceTime;

import java.util.UUID;

@Repository
public interface UserRoomEntranceTimeRepository extends JpaRepository<UserRoomEntranceTime, Long> {
    UserRoomEntranceTime findByRoomNameAndUserUuid(String roomName, UUID userUuid);
}
