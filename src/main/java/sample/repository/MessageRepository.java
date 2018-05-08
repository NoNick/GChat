package sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sample.model.Message;

import java.util.Set;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Set<Message> findAllByUserRank(Integer userRank);

    Set<Message> findAllByRoomName(String roomName);

    Set<Message> findAllBySecret(boolean isSecret);
}
