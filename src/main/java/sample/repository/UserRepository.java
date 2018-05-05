package sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sample.model.Room;
import sample.model.User;

import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Set<User> findAllByUserRoomsContains(Room room);
}
