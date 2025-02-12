package com.rescueme.repository;

import com.rescueme.repository.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByShelterId(Long shelterId);

    @Query("SELECT p FROM Pet p LEFT JOIN FETCH p.photos WHERE p.shelter.id = :shelterId")
    List<Pet> findByShelterIdWithPhotos(@Param("shelterId") Long shelterId);
}
