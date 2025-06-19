package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.home.automation.entity.Rooms;
import project.home.automation.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, String> {
    Rooms findTopByOrderByRoomIdDesc();
    boolean existsByRoomNameIgnoreCaseAndUser_UserId(String roomName, String userId);
    int countByUser_UserId(String userId);
    List<Rooms> findByUser_UserId(String userId);
    Optional<Rooms> findByRoomIdAndUser_UserId(String roomId, String userId);
    Optional<Rooms> findByRoomNameAndUser(String roomName, User userObj);
    List<Rooms> findByUser(User userObj);
}