package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.home.automation.entity.Device;
import project.home.automation.entity.Room;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByRoom(Room room);
    List<Device> findByRoom_RoomId(String roomId);
    Device findTopByOrderByDeviceIdDesc();
    Optional<Device> findByThingUIDAndUser_UserId(String thingUID, String userId);
}