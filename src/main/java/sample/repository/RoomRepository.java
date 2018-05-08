package sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sample.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

}
