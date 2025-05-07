package project.home.automation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.home.automation.dto.ChangePasswordDTO;
import project.home.automation.dto.OtpDTO;
import project.home.automation.dto.UserDTO;
import project.home.automation.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Post register
    @PostMapping("/register")
    public ResponseEntity<?> postRegister(@RequestBody UserDTO registerRequest) {
        return userService.postRegister(registerRequest);
    }

    // Post login
    @PostMapping("/login")
    public ResponseEntity<?> postLogin(@RequestBody UserDTO loginRequest) {
        return userService.postLogin(loginRequest);
    }

    // Post email
    @PostMapping("/forgot/password")
    public ResponseEntity<?> postEmail(@RequestBody OtpDTO emailRequest) {
        return userService.postEmail(emailRequest);
    }

    // Post OTP
    @PostMapping("/otp/verify")
    public ResponseEntity<?> postOtp(@RequestBody OtpDTO otpRequest) {
        return userService.postOtp(otpRequest);
    }

    @PutMapping("/reset/password")
    public ResponseEntity<?> putPassword(@RequestBody OtpDTO updateRequest) {
        return userService.putPassword(updateRequest);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        return userService.getProfile(token);
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> putUpdateProfile(@RequestHeader("Authorization") String token, @RequestBody UserDTO updateRequest) {
        return userService.putUpdateProfile(token, updateRequest);
    }

    @PostMapping("/password/verify")
    public ResponseEntity<?> postPasswordAndGetOtp(@RequestHeader("Authorization") String token, @RequestBody OtpDTO otpRequest) {
        return userService.postPasswordAndGetOtp(token, otpRequest);
    }

    @PutMapping("/change/password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody ChangePasswordDTO updateRequest) {
        return userService.changePassword(token, updateRequest);
    }
}