package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.home.automation.entity.Rules;
import project.home.automation.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RulesRepository extends JpaRepository<Rules, String> {
    Rules findTopByOrderByRuleIdDesc();
    List<Rules> findByUser(User user);
    Optional<Rules> findByRuleIdAndUser(String scenesId, User user);
    List<Rules> findAllByThingIdAndUser(String thingUID, User userObj);
}