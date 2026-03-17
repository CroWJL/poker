package com.example.poker.player;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record LoginRequest(String nickname) {}

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserService.LoginResponse> login(@RequestBody LoginRequest request) {
        UserService.LoginResponse response = userService.loginOrRegister(request.nickname());
        return ResponseEntity.ok(response);
    }
}

