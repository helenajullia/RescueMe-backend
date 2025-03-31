package com.rescueme.service;

import java.util.List;

public interface FavoriteService {

    int getFavoritesCount(Long userId);

    List<Long> getFavorites(Long userId);

    void addFavorite(Long userId, Long petId);

    void removeFavorite(Long userId, Long petId);

    boolean isFavorite(Long userId, Long petId);
}