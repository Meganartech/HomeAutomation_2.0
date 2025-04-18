package project.home.automation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.home.automation.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User findTopByOrderByUserIdDesc();
    Optional<User> findByUserId(String userId);
    void deleteByUserId(String userId);
}