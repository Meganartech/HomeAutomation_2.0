package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.home.automation.entity.Thing;
import project.home.automation.entity.User;

import java.util.List;
import java.util.Optional;

public interface ThingRepository extends JpaRepository<Thing, Integer> {
    Optional<Thing> findByThingUID(String thingUID);
    Optional<Thing> findByThingUIDAndUser(String thingUID, User user);
}