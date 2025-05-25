package com.rescueme.security;

import com.rescueme.repository.RefreshTokenRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.ShelterStatus;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Map<String, String> resetTokens = new HashMap<>();

    /**
     * Registers a new adopter account
     */
    @PostMapping("/register/adopter")
    public ResponseEntity<Map<String, String>> registerAdopter(@RequestBody AdopterRegisterRequest registerRequest) {
        userService.addAdopter(registerRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Adopter registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Registers a new shelter account and returns the assigned shelter ID
     */
    @PostMapping("/register/shelter")
    public ResponseEntity<Map<String, Object>> registerShelter(@RequestBody ShelterRegisterRequest registerRequest) {
        Long Id = userService.addShelter(registerRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Shelter registered successfully");
        response.put("Id", Id);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Logs in a user and sets JWT access and refresh tokens as secure cookies
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        Cookie accessCookie = new Cookie("accessToken", loginResponse.getToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15);

        Cookie refreshCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);


        if (user.getRole() == Role.SHELTER) {
            loginResponse.setStatus(user.getStatus().toString());

            if (user.getStatus() == ShelterStatus.REJECTED) {
                loginResponse.setRejectionReason(user.getRejectionReason());
                loginResponse.setRejectionDetails(user.getRejectionDetails());
                loginResponse.setRejectedAt(user.getRejectedAt() != null ?
                        user.getRejectedAt().toString() : null);
            }

            loginResponse.setFirstLoginAfterApproval(user.getFirstLoginAfterApproval());
        }

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Logs out the user by removing access and refresh tokens from cookies and revoking the refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response,
                                    @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
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

    /**
     * Refreshes the access token using a valid refresh token from any source
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody(required = false) Map<String, String> body) {

        String refreshToken = null;

        if (body != null && body.containsKey("refreshToken")) {
            refreshToken = body.get("refreshToken");
        }

        if (refreshToken == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
            }
        }

        if (refreshToken == null || !authService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired refresh token"));
        }

        String email = authService.extractUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }

        String newAccessToken = authService.generateAccessToken(user);

        Cookie accessCookie = new Cookie("accessToken", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15);
        response.addCookie(accessCookie);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Access token refreshed");
        responseBody.put("token", newAccessToken);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/test-refresh")
    public ResponseEntity<?> testRefresh() {
        return ResponseEntity.ok(Map.of("message", "This is a protected resource, if you see this, authentication works"));
    }

    /**
     * Checks whether the given email address is already registered
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        Map<String, Object> response = new HashMap<>();
        response.put("emailExists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * Checks whether the given username is already taken
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        Map<String, Object> response = new HashMap<>();
        response.put("usernameExists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a password reset email with a unique token to the user
     */
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

    /**
     * Resets the password using a valid token previously sent via email
     */
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

    /**
     * Deletes a rejected shelter.
     */
    @DeleteMapping("/delete-rejected-shelter")
    public ResponseEntity<Map<String, String>> deleteRejectedShelter(@RequestBody Map<String, Object> request) {
        try {
            String shelterIdStr = String.valueOf(request.get("shelterId"));
            Long shelterId = Long.parseLong(shelterIdStr);

            User shelter = userRepository.findById(shelterId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

            if (shelter.getRole() == Role.SHELTER) {
                refreshTokenRepository.deleteByUserId(shelterId);

                userRepository.delete(shelter);

                return ResponseEntity.ok(Map.of("message", "Shelter deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Only shelters can be deleted through this endpoint"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid shelter ID format"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete shelter: " + e.getMessage()));
        }
    }
}
