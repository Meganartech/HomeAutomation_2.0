package project.home.automation.controller;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.home.automation.dto.*;
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

    // Get profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        return userService.getProfile(token);
    }

    // Put update
    @PutMapping("/update")
    public ResponseEntity<?> putUpdate(@RequestHeader("Authorization") String token, @RequestBody UserDTO registerRequest) {
        return userService.putUpdate(token, registerRequest);
    }

    // Post room
    @PostMapping("/room")
    public ResponseEntity<?> postRoom(@RequestHeader("Authorization") String token, @RequestBody RoomDTO thingRequest) {
        return userService.postRoom(token, thingRequest);
    }

    // Get room list
    @GetMapping("/room/list")
    public ResponseEntity<?> getRoomList(@RequestHeader("Authorization") String token) {
        return userService.getRoomList(token);
    }

    // Post thing
    @PostMapping("/thing")
    public ResponseEntity<?> postThing(@RequestHeader("Authorization") String token, @RequestBody ThingDTO thingRequest) {
        return userService.postThing(token, thingRequest);
    }

    // Get thing list
    @GetMapping("/thing/list")
    public ResponseEntity<?> getThingList(@RequestHeader("Authorization") String token) {
        return userService.getThing(token);
    }

    // Post scan
    @PostMapping("/scan")
    public ResponseEntity<?> postScan(@RequestHeader("Authorization") String token) {
        return userService.postScan(token);
    }

    // Get inbox
    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(@RequestHeader("Authorization") String token) {
        return userService.getInbox(token);
    }

    // Forgot password - Send OTP
    @PostMapping("/forgot/password")
    public ResponseEntity<?> postOtp(@RequestBody OtpDTO request) {
        return userService.postOtp(request);
    }

    // Forgot password - Verify OTP
    @PostMapping("/verify/otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpDTO request) {
        return userService.verifyOtp(request);
    }

    // Reset password
    @PostMapping("/reset/password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO request) {
        return userService.resetPassword(request);
    }

}