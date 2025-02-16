package com.rescueme.repository;

import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    int countByStatus(PetStatus petStatus);

    List<Pet> findByShelterId(Long shelterId);

    @Query("SELECT p FROM Pet p LEFT JOIN FETCH p.photos WHERE p.shelter.id = :shelterId")
    List<Pet> findByShelterIdWithPhotos(@Param("shelterId") Long shelterId);
}
