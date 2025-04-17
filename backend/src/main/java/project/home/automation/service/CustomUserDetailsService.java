package project.home.automation.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.home.automation.entity.User;
import project.home.automation.repository.UserRepository;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> usersData = userRepository.findByUsername(username);
        if (usersData.isPresent()) {
            User userObj = usersData.get();
            return new org.springframework.security.core.userdetails.User(userObj.getUsername(), userObj.getPassword(), userObj.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getRoleName())).collect(Collectors.toList()));
        }
        throw new UsernameNotFoundException("User not found with this username: " + username);
    }
}