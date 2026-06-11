package com.rescueme.controller;

import com.rescueme.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Returns the number of favorite pets for a specific user
     */
    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Integer>> getFavoritesCount(@PathVariable Long userId) {
        int count = favoriteService.getFavoritesCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Returns a list of pet IDs marked as favorite by the user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<Long>> getFavorites(@PathVariable Long userId) {
        List<Long> favorites = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    /**
     * Adds a pet to the user's list of favorites
     */
    @PostMapping("/{userId}/{petId}")
    @PreAuthorize("hasRole('ROLE_ADOPTER')")
    public ResponseEntity<Map<String, String>> addFavorite(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        favoriteService.addFavorite(userId, petId);
        return ResponseEntity.ok(Map.of("message", "Pet added to favorites"));
    }

    /**
     * Removes a pet from the user's list of favorites
     */
    @DeleteMapping("/{userId}/{petId}")
    @PreAuthorize("hasRole('ROLE_ADOPTER')")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        favoriteService.removeFavorite(userId, petId);
        return ResponseEntity.ok(Map.of("message", "Pet removed from favorites"));
    }

    /**
     * Checks if a pet is marked as favorite by the user
     */
    @GetMapping("/check/{userId}/{petId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        boolean isFavorite = favoriteService.isFavorite(userId, petId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }
}