package com.rescueme.controller;

import com.rescueme.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Integer>> getFavoritesCount(@PathVariable Long userId) {
        int count = favoriteService.getFavoritesCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Long>> getFavorites(@PathVariable Long userId) {
        List<Long> favorites = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{userId}/{petId}")
    public ResponseEntity<Map<String, String>> addFavorite(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        favoriteService.addFavorite(userId, petId);
        return ResponseEntity.ok(Map.of("message", "Pet added to favorites"));
    }

    @DeleteMapping("/{userId}/{petId}")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        favoriteService.removeFavorite(userId, petId);
        return ResponseEntity.ok(Map.of("message", "Pet removed from favorites"));
    }

    @GetMapping("/check/{userId}/{petId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        boolean isFavorite = favoriteService.isFavorite(userId, petId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }
}