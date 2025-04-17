package project.home.automation.controller;

import project.home.automation.dto.UserDTO;
import project.home.automation.service.AdminService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Post login
    @PostMapping("/login")
    public ResponseEntity<?> postLogin(@RequestBody UserDTO loginRequest) {
        return adminService.postLogin(loginRequest);
    }

    // Get profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        return adminService.getProfile(token);
    }

    // Put update
    @PutMapping("/update")
    public ResponseEntity<?> putUpdate(@RequestHeader("Authorization") String token, @RequestBody UserDTO updateRequest) {
        return adminService.putUpdate(token, updateRequest);
    }

    // Get user list
    @GetMapping("/user/list")
    public ResponseEntity<?> getUserList(@RequestHeader("Authorization") String token) {
        return adminService.getUserList(token);
    }

    // Delete user
    @Transactional
    @DeleteMapping("/user/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token, @RequestParam String userId) {
        return adminService.deleteUser(token, userId);
    }
}