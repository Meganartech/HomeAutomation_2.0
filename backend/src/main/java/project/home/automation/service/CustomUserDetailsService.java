package project.home.automation.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import project.home.automation.entity.User;
import project.home.automation.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> usersData = userRepository.findByEmail(email);
        if (usersData.isPresent()) {
            User userObj = usersData.get();
            return new org.springframework.security.core.userdetails.User(userObj.getEmail(), userObj.getPassword(), Collections.singleton(new SimpleGrantedAuthority(userObj.getRole())));
        }
        throw new UsernameNotFoundException(email + " - email is not found");
    }
}