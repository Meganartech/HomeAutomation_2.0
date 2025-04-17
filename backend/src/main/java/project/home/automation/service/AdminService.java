package project.home.automation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import project.home.automation.dto.UserDTO;
import project.home.automation.entity.Role;
import project.home.automation.entity.User;
import project.home.automation.repository.UserRepository;
import project.home.automation.security.JwtUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AdminService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Value("${role.admin}")
    private String roleAdmin;

    @Value("${role.user}")
    private String roleUser;

    // Post login
    public ResponseEntity<?> postLogin(UserDTO loginRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // Generate token
            String token = jwtUtil.generateToken(loginRequest.getUsername());

            // Fetch user to get role
            Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "User not found"));
            }

            // Extract role names
            Set<Role> roles = user.get().getRoles();
            String roleName = roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.joining(",")); // "USER", or "ADMIN,USER" if multiple roles

            // Prepare response
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", roleName);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Get profile
    public ResponseEntity<?> getProfile(String token) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleAdmin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }
            // Extract username
            String username = jwtUtil.extractUsername(jwtToken);
            Optional<User> adminData = userRepository.findByUsername(username);
            if (adminData.isPresent()) {
                // Return actual staff object
                return ResponseEntity.ok(adminData.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Profile not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong" + e.getMessage()));
        }
    }

    // Put update
    public ResponseEntity<?> putUpdate(String token, UserDTO updateRequest) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleAdmin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }
            // Extract username
            String username = jwtUtil.extractUsername(jwtToken);
            Optional<User> adminData = userRepository.findByUsername(username);
            if (adminData.isPresent()) {
                User adminObj = adminData.get();
                // Update only non-null values
                if (updateRequest.getName() != null && !updateRequest.getName().isEmpty()) {
                    adminObj.setName(updateRequest.getName());
                }
                if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
                    adminObj.setEmail(updateRequest.getEmail());
                }
                userRepository.save(adminObj);
                return ResponseEntity.ok(Collections.singletonMap("message", "Profile updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong" + e.getMessage()));
        }
    }

    // Get user list
    public ResponseEntity<?> getUserList(String token) {
        // Check if token is null or doesn't start with "Bearer "
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix to extract the actual JWT token
        String jwtToken = token.substring(7);
        try {
            // Validate the JWT token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract roles from token and check if the user is an admin
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleAdmin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Access denied"));
            }
            // Fetch all users from the database
            List<User> userList = userRepository.findAll();
            // Filter users who have the "USER" role
            List<User> userRoleList = userList.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getRoleName().equals("USER")))
                    .collect(Collectors.toList());
            // Return the filtered list
            return ResponseEntity.ok(userRoleList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Delete user
    public ResponseEntity<?> deleteUser(String token, String userId) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleAdmin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }
            Optional<User> staffOptional = userRepository.findByUserId(userId);
            if (staffOptional.isPresent()) {
                userRepository.deleteByUserId(userId);
                return ResponseEntity.ok(Collections.singletonMap("message", "User deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong" + e.getMessage()));
        }
    }
}