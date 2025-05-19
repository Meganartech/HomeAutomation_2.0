package project.home.automation.controller;

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

    @PostMapping("/room")
    public ResponseEntity<?> postRoom(@RequestHeader("Authorization") String token, @RequestBody RoomDTO registerRequest) {
        return userService.postRoom(token, registerRequest);
    }

    @GetMapping("/room/list")
    public ResponseEntity<?> getRoom(@RequestHeader("Authorization") String token) {
        return userService.getRoom(token);
    }

    @PostMapping("/scan")
    public ResponseEntity<?> postScan(@RequestHeader("Authorization") String token, @RequestParam String binding) {
        return userService.postScan(token, binding);
    }

    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(@RequestHeader("Authorization") String token) {
        return userService.getInbox(token);
    }

//    @PostMapping("/thing")
//    public ResponseEntity<?> postThing(@RequestHeader("Authorization") String token, @RequestBody ThingDTO thingRequest) {
//        return userService.postThing(token, thingRequest);
//    }

//    @GetMapping("/channels")
//    public ResponseEntity<?> getChannel(@RequestHeader("Authorization") String token, @RequestParam String thingUID) {
//        return userService.getChannel(token, thingUID);
//    }

//    @PostMapping("/test")
//    public ResponseEntity<?> autoCreateAndLinkItems(@RequestHeader("Authorization") String token, @RequestParam String thingUID) {
//        return userService.autoCreateAndLinkItems(token, thingUID);
//    }

    @PostMapping("/test")
    public ResponseEntity<?> addDeviceAndLinkItems(@RequestHeader("Authorization") String token, @RequestBody ThingDTO thingRequest) {
        return userService.addDeviceAndLinkItems(token, thingRequest);
    }
}