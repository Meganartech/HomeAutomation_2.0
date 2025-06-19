package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.home.automation.entity.Things;
import project.home.automation.entity.Rooms;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThingsRepository extends JpaRepository<Things, String> {
    Things findTopByOrderByThingIdDesc();
    List<Things> findByRooms(Rooms rooms);
    List<Things> findByRooms_RoomId(String roomId);
    Optional<Things> findByThingUIDAndUser_UserId(String thingUID, String userId);
}