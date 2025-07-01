package project.home.automation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.home.automation.dto.*;
import project.home.automation.service.UserService1;

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
    public ResponseEntity<?> postRegister(@RequestBody UserDTO postRequest) {
        return userService.postRegister(postRequest);
    }

    // Post login
    @PostMapping("/login")
    public ResponseEntity<?> postLogin(@RequestBody UserDTO postRequest) {
        return userService.postLogin(postRequest);
    }

    // Post email and Get OTP
    @PostMapping("/forgot/password")
    public ResponseEntity<?> postEmail_1(@RequestBody MailDTO postRequest) {
        return userService.postEmail_1(postRequest);
    }

    // Post email and Get OTP (resend)
    @PostMapping("/resend/otp")
    public ResponseEntity<?> postEmail_2(@RequestBody MailDTO postRequest){
        return userService.postEmail_2(postRequest);
    }

    // Post OTP
    @PostMapping("/otp/verify")
    public ResponseEntity<?> postOtp(@RequestBody MailDTO postRequest) {
        return userService.postOtp(postRequest);
    }

    // Patch password 1
    @PatchMapping("/reset/password")
    public ResponseEntity<?> patchPassword_1(@RequestBody MailDTO patchRequest) {
        return userService.patchPassword_1(patchRequest);
    }

    // Get profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        return userService.getProfile(token);
    }

    // Post password
    @PostMapping("/password/verify")
    public ResponseEntity<?> postPasswordAndGetOtp(@RequestHeader("Authorization") String token, @RequestBody MailDTO postRequest) {
        return userService.postPasswordAndGetOtp(token, postRequest);
    }

    // Patch profile
    @PatchMapping("/profile/update")
    public ResponseEntity<?> patchProfile(@RequestHeader("Authorization") String token, @RequestBody UserDTO patchRequest) {
        return userService.patchProfile(token, patchRequest);
    }

    // Patch password 2
    @PatchMapping("/change/password")
    public ResponseEntity<?> patchPassword_2(@RequestHeader("Authorization") String token, @RequestBody ChangePasswordDTO patchRequest) {
        return userService.patchPassword_2(token, patchRequest);
    }

    // Post room
    @PostMapping("/room")
    public ResponseEntity<?> postRoom(@RequestHeader("Authorization") String token, @RequestBody RoomsDTO postRequest) {
        return userService.postRoom(token, postRequest);
    }

    // Get room
    @GetMapping("/room")
    public ResponseEntity<?> getRoom(@RequestHeader("Authorization") String token) {
        return userService.getRoom(token);
    }

    // Delete room
    @DeleteMapping("/delete/room")
    public ResponseEntity<?> deleteRoom(@RequestHeader("Authorization") String token, @RequestParam String roomId) {
        return userService.deleteRoom(token, roomId);
    }

    // Post scan and Get inbox
    @PostMapping("/scan")
    public ResponseEntity<?> postScanAndGetInbox(@RequestHeader("Authorization") String token, @RequestParam String binding) {
        return userService.postScanAndGetInbox(token, binding);
    }

    // Post thing and link items
    @PostMapping("/thing")
    public ResponseEntity<?> postThingAndLinkItems(@RequestHeader("Authorization") String token, @RequestBody ThingsDTO postRequest) {
        return userService.postThingAndLinkItems(token, postRequest);
    }

    // Get thing and items
    @GetMapping("/thing")
    public ResponseEntity<?> getThingWithItems(@RequestHeader("Authorization") String token) {
        return userService.getThingWithItems(token);
    }

    // Delete thing
    @DeleteMapping("/delete/thing")
    public ResponseEntity<?> deleteThing(@RequestHeader("Authorization") String token, @RequestParam String thingUID) {
        return userService.deleteThing(token, thingUID);
    }

    // Post control
    @PostMapping("/control")
    public ResponseEntity<?> postControl(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> payload) {
        return userService.postControl(token, payload);
    }

    // Post rule
    @PostMapping("/rule")
    public ResponseEntity<?> postRule(@RequestHeader("Authorization") String token, @RequestBody RulesDTO postRequest) {
        return userService.postRule(token, postRequest);
    }

    // Get rule
    @GetMapping("/rule")
    public ResponseEntity<?> getRule(@RequestHeader("Authorization") String token) {
        return userService.getRule(token);
    }

    // Patch rule
    @PatchMapping("/rule/update")
    public ResponseEntity<?> patchRule(@RequestHeader("Authorization") String token, @RequestBody RulesDTO patchRules) {
        return userService.patchRule(token, patchRules);
    }

    // Enable or disable rule
    @PatchMapping("/rule/toggle")
    public ResponseEntity<?> toggleRule(@RequestHeader("Authorization") String token, @RequestBody RulesToggleDTO dto) {
        return userService.toggleRule(token, dto);
    }

    // Delete rule
    @DeleteMapping("/delete/rule")
    public ResponseEntity<?> deleteRule(@RequestHeader("Authorization") String token, @RequestParam String ruleId) {
        return userService.deleteRule(token, ruleId);
    }

}