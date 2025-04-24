package com.rescueme.security;

import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.User;
import com.rescueme.security.request.AdopterRegisterRequest;
import com.rescueme.security.request.LoginRequest;
import com.rescueme.security.request.ShelterRegisterRequest;
import com.rescueme.security.response.LoginResponse;
import com.rescueme.service.EmailService;
import com.rescueme.service.UserService;
import com.rescueme.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> resetTokens = new HashMap<>();

    @PostMapping("/register/adopter")
    public ResponseEntity<Map<String, String>> registerAdopter(@RequestBody AdopterRegisterRequest registerRequest) {
        userService.addAdopter(registerRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Adopter registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/shelter")
    public ResponseEntity<Map<String, Object>> registerShelter(@RequestBody ShelterRegisterRequest registerRequest) {
        Long Id = userService.addShelter(registerRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Shelter registered successfully");
        response.put("Id", Id);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        Cookie accessCookie = new Cookie("accessToken", loginResponse.getToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15);

        Cookie refreshCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        authService.saveRefreshToken(loginResponse.getRefreshToken(), user, 1000L * 60 * 60 * 24 * 7);

        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response,
                                    @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        System.out.println("Refresh token from cookie: " + refreshToken);

        if (refreshToken != null) {
            authService.revokeRefreshToken(refreshToken);
        }

        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                          HttpServletResponse response) {
        if (refreshToken == null || !authService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired refresh token"));
        }

        String email = authService.extractUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }

        String newAccessToken = authService.generateAccessToken(user);

        Cookie accessCookie = new Cookie("accessToken", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15);
        response.addCookie(accessCookie);

        return ResponseEntity.ok(Map.of("message", "Access token refreshed"));
    }


    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        Map<String, Object> response = new HashMap<>();
        response.put("emailExists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        Map<String, Object> response = new HashMap<>();
        response.put("usernameExists", exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        emailService.sendEmail(user.getEmail(), "Reset Your Password",
                "Hello " + user.getUsername() + ",\n\n" +
                        "You recently requested to reset your password for your RescueMe Site account. Click the link below to proceed:\n\n" +
                        resetLink + "\n\n" +
                        "Your password won't change until you access the link above and create a new one.\n\n"+
                        "If you didn't request this, ignore this email.\n\n" +
                        "Thanks,\n\nRescueMe Team");

        return ResponseEntity.ok(Map.of("message", "Password reset email sent!"));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        String email = resetTokens.get(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid or expired token"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokens.remove(token);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

}
