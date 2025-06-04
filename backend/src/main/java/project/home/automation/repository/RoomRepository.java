package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.home.automation.entity.Room;
import project.home.automation.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    Room findTopByOrderByRoomIdDesc();
    boolean existsByRoomNameIgnoreCaseAndUser_UserId(String roomName, String userId);
    int countByUser_UserId(String userId);
    List<Room> findByUser_UserId(String userId);
    Optional<Room> findByRoomIdAndUser_UserId(String roomId, String userId);
    Optional<Room> findByRoomNameAndUser(String roomName, User userObj);
    List<Room> findByUser(User userObj);
    List<Room> findByRoomIdIn(Set<String> roomIds);
}