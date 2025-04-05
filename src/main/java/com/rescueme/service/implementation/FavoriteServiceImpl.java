package com.rescueme.service.implementation;

import com.rescueme.repository.FavoriteRepository;
import com.rescueme.repository.PetRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.Favorite;
import com.rescueme.service.FavoriteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    @Override
    public int getFavoritesCount(Long userId) {
        validateUser(userId);
        return (int) favoriteRepository.countByUserId(userId);
    }

    @Override
    public List<Long> getFavorites(Long userId) {
        validateUser(userId);
        return favoriteRepository.findPetIdsByUserId(userId);
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long petId) {
        validateUser(userId);
        validatePet(petId);

        if (favoriteRepository.findByUserIdAndPetId(userId, petId).isPresent()) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setPetId(petId);

        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long petId) {
        validateUser(userId);
        favoriteRepository.deleteByUserIdAndPetId(userId, petId);
    }

    @Override
    public boolean isFavorite(Long userId, Long petId) {
        validateUser(userId);
        return favoriteRepository.findByUserIdAndPetId(userId, petId).isPresent();
    }


    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId);
        }
    }

    private void validatePet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with ID: " + petId);
        }
    }
}