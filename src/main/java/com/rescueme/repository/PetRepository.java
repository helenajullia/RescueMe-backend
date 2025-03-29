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

    List<Pet> findByShelterId(Long shelterId);;

    int countByShelterIdAndStatus(Long shelterId, PetStatus status);

    @Query("SELECT DISTINCT p.breed FROM Pet p WHERE p.breed IS NOT NULL ORDER BY p.breed ASC")
    List<String> findDistinctBreeds();


    @Query("SELECT DISTINCT p.breed FROM Pet p WHERE p.species = :species AND p.breed IS NOT NULL ORDER BY p.breed ASC")
    List<String> findDistinctBreedsBySpecies(@Param("species") String species);

    long countByShelterId(Long shelterId);

}
