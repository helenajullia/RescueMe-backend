package com.rescueme.security;

import com.rescueme.security.request.AdopterRegisterRequest;
import com.rescueme.security.request.LoginRequest;
import com.rescueme.security.request.ShelterRegisterRequest;
import com.rescueme.security.response.LoginResponse;
import com.rescueme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register/adopter")
    public ResponseEntity<Map<String, String>> registerAdopter(@RequestBody AdopterRegisterRequest registerRequest) {
        userService.addAdopter(registerRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Adopter registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/shelter")
    public ResponseEntity<Map<String, String>> registerShelter(@RequestBody ShelterRegisterRequest registerRequest) {
        userService.addShelter(registerRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shelter registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}
