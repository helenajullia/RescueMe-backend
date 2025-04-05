package com.rescueme.repository;

import com.rescueme.repository.entity.AdoptionRequest;
import com.rescueme.repository.entity.AdoptionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, String> {
    List<AdoptionRequest> findByUserId(Long userId);

    @Query("SELECT ar FROM AdoptionRequest ar WHERE ar.pet.shelter.id = :shelterId")
    List<AdoptionRequest> findByShelterIdOrderByRequestDateDesc(Long shelterId);
    List<AdoptionRequest> findByPetId(Long petId);
    List<AdoptionRequest> findByUserIdAndStatus(Long userId, AdoptionRequestStatus status);
    @Query("SELECT ar FROM AdoptionRequest ar WHERE ar.pet.shelter.id = :shelterId AND ar.status = :status")
    List<AdoptionRequest> findByShelterIdAndStatus(Long shelterId, AdoptionRequestStatus status);
    List<AdoptionRequest> findByPetIdAndStatus(Long petId, AdoptionRequestStatus status);
    boolean existsByUserIdAndPetId(Long userId, Long petId);
    Optional<AdoptionRequest> findByUserIdAndPetId(Long userId, Long petId);
}