package com.rescueme.controller;

import com.rescueme.repository.dto.UserDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.ShelterStatus;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetService;
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
    private final PetService petService;

    /**
     * Returns user details by user ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Returns a list of all users in the system
     */
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Deletes a user by their ID
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    /**
     * Updates a user by their ID
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> updates) {
        User updatedUser = userService.updateUser(userId, updates);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Uploads a profile picture for the user
     */
    @PostMapping("/{id}/uploadProfilePicture")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        userService.uploadProfilePicture(id, file);
        return ResponseEntity.ok("Profile picture uploaded successfully");
    }

    /**
     * Returns the profile picture for the user in JPEG format
     */
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

    /**
     * Deletes the profile picture of the user
     */
    @DeleteMapping("/{id}/profilePicture")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable Long id) {
        userService.deleteProfilePicture(id);
        return ResponseEntity.ok("Profile picture deleted successfully");
    }

    /**
     * Changes the password of a user given the current and new password
     */
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

    /**
     * Returns shelter information by ID
     */
    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<User> getShelterById(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);
        return ResponseEntity.ok(shelter);
    }

    /**
     * Returns a list of all shelters as UserDTOs
     */
    @GetMapping("/shelters")
    public ResponseEntity<List<UserDTO>> getAllShelters() {
        List<UserDTO> shelters = userService.getAllShelters();
        return ResponseEntity.ok(shelters);
    }

    /**
     * Returns the shelter that owns a specific pet
     */
    @GetMapping("/shelter/by-pet/{petId}")
    public ResponseEntity<User> getShelterByPetId(@PathVariable Long petId) {
        try {
            Pet pet = petService.getPetById(petId);
            if (pet == null) {
                return ResponseEntity.notFound().build();
            }

            User shelter = pet.getShelter();
            return ResponseEntity.ok(shelter);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns a list of shelters filtered by status
     */
    @GetMapping("/shelters/status/{status}")
    public ResponseEntity<List<UserDTO>> getSheltersByStatus(@PathVariable ShelterStatus status) {
        List<UserDTO> shelters = userService.getSheltersByStatus(status);
        return ResponseEntity.ok(shelters);
    }

    /**
     * Returns a list of approved shelters only
     */
    @GetMapping("/shelters/approved")
    public ResponseEntity<List<UserDTO>> getApprovedShelters() {
        List<UserDTO> approvedShelters = userService.getApprovedShelters();
        return ResponseEntity.ok(approvedShelters);
    }
}
