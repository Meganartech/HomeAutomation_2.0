package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.home.automation.entity.Room;
import project.home.automation.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByRoomNameAndUser(String roomName, User user);
    List<Room> findByUser(User user);
    Optional<Room> findByRoomId(int roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Room n WHERE n.roomId = :roomId")
    void deleteByRoomId(int roomId);
}