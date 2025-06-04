package project.home.automation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.home.automation.dto.*;
import project.home.automation.service.UserService1;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService1 userService;

    public UserController(UserService1 userService) {
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

    // Patch password update 1
    @PatchMapping("/reset/password")
    public ResponseEntity<?> patchPassword_1Update(@RequestBody OtpDTO updateRequest) {
        return userService.patchPassword_1Update(updateRequest);
    }

    // Get profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        return userService.getProfile(token);
    }

    // Patch profile update
    @PatchMapping("/profile/update")
    public ResponseEntity<?> patchProfileUpdate(@RequestHeader("Authorization") String token, @RequestBody UserDTO updateRequest) {
        return userService.patchProfileUpdate(token, updateRequest);
    }

    // Patch password update 2
    @PatchMapping("/change/password")
    public ResponseEntity<?> patchPassword_2Update(@RequestHeader("Authorization") String token, @RequestBody ChangePasswordDTO updateRequest) {
        return userService.patchPassword_2Update(token, updateRequest);
    }

    // Post password
    @PostMapping("/password/verify")
    public ResponseEntity<?> postPasswordAndGetOtp(@RequestHeader("Authorization") String token, @RequestBody OtpDTO otpRequest) {
        return userService.postPasswordAndGetOtp(token, otpRequest);
    }

    // Post room
    @PostMapping("/room")
    public ResponseEntity<?> postRoom(@RequestHeader("Authorization") String token, @RequestBody RoomDTO registerRequest) {
        return userService.postRoom(token, registerRequest);
    }

    // Get room
    @GetMapping("/room")
    public ResponseEntity<?> getRoom(@RequestHeader("Authorization") String token) {
        return userService.getRoom(token);
    }

    // Delete room
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<?> deleteRoom(@RequestHeader("Authorization") String token, @PathVariable String roomId) {
        return userService.deleteRoom(token, roomId);
    }

    // Post scan and get inbox
    @PostMapping("/scan")
    public ResponseEntity<?> postScan(@RequestHeader("Authorization") String token, @RequestParam String binding) {
        return userService.postScanAndGetInbox(token, binding);
    }

    // Post thing and link items
    @PostMapping("/thing")
    public ResponseEntity<?> addThingAndLinkItems(@RequestHeader("Authorization") String token, @RequestBody ThingDTO postRequest) {
        return userService.addThingAndLinkItems(token, postRequest);
    }

    // Get thing and items
    @GetMapping("/device")
    public ResponseEntity<?> getThingsWithItems(@RequestHeader("Authorization") String token) {
        return userService.getThingsWithItems(token);
    }

    // Delete device
    @DeleteMapping("/device/{thingUID}")
    public ResponseEntity<?> deleteDevice(@RequestHeader("Authorization") String token, @PathVariable String thingUID) {
        return userService.deleteDevice(token, thingUID);
    }

    // Post control
    @PostMapping("/control")
    public ResponseEntity<?> controlItem(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> payload) {
        return userService.postControl(token, payload);
    }

    // Post scenes
    @PostMapping("/scenes")
    public ResponseEntity<?> postScenes(@RequestHeader("Authorization") String token, @RequestBody ScenesDTO postRequest) {
        return userService.postScenes(token, postRequest);
    }

    // Get scenes
    @GetMapping("/scenes")
    public ResponseEntity<?> getScenes(@RequestHeader("Authorization") String token) {
        return userService.getScenes(token);
    }

    // Patch scenes
    @PatchMapping("/scenes/update")
    public ResponseEntity<?> patchScenesAndRules(@RequestHeader("Authorization") String token, @RequestBody List<ScenesDTO> updatedScenesList) {
        return userService.patchScenesAndRules(token, updatedScenesList);
    }

}