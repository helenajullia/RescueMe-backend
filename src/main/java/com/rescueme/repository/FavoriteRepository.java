package com.rescueme.repository;

import com.rescueme.repository.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    long countByUserId(Long userId);
    List<Favorite> findByUserId(Long userId);
    Optional<Favorite> findByUserIdAndPetId(Long userId, Long petId);
    @Query("SELECT f.petId FROM Favorite f WHERE f.userId = :userId")
    List<Long> findPetIdsByUserId(@Param("userId") Long userId);
    void deleteByUserIdAndPetId(Long userId, Long petId);
}