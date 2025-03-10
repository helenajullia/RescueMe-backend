package com.rescueme.controller;

import com.rescueme.repository.dto.UserDTO;
import com.rescueme.repository.entity.User;
import com.rescueme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> updates) {
        User updatedUser = userService.updateUser(userId, updates);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/uploadProfilePicture")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        userService.uploadProfilePicture(id, file);
        return ResponseEntity.ok("Profile picture uploaded successfully");
    }

    @GetMapping("/{id}/profilePicture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long id) {
        byte[] image = userService.getProfilePicture(id);

        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    @DeleteMapping("/{id}/profilePicture")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable Long id) {
        userService.deleteProfilePicture(id);
        return ResponseEntity.ok("Profile picture deleted successfully");
    }

    @PatchMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long userId, @RequestBody Map<String, String> passwords) {
        try {
            userService.changePassword(userId, passwords.get("currentPassword"), passwords.get("newPassword"));
            return ResponseEntity.ok(Collections.singletonMap("message", "Password changed successfully"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Collections.singletonMap("message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "An unexpected error occurred"));
        }
    }

    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<User> getShelterById(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);
        return ResponseEntity.ok(shelter);
    }

    @GetMapping("/shelters")
    public ResponseEntity<List<UserDTO>> getAllShelters() {
        List<UserDTO> shelters = userService.getAllShelters();
        return ResponseEntity.ok(shelters);
    }



}
