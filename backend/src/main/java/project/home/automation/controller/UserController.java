package project.home.automation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.home.automation.dto.UserDTO;
import project.home.automation.service.UserService;

@RestController
@RequestMapping("/user")
@CrossOrigin("http://localhost:3000")
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

}