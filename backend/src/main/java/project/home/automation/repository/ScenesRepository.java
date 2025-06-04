package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.home.automation.entity.Scenes;
import project.home.automation.entity.User;

import java.util.List;
import java.util.Optional;


@Repository
public interface ScenesRepository extends JpaRepository<Scenes, String> {
    Scenes findTopByOrderByScenesIdDesc();
    List<Scenes> findByUser(User user);
    Optional<Scenes> findByScenesIdAndUser(String scenesId, User user);
}