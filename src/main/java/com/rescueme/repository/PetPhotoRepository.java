package com.rescueme.repository;

import com.rescueme.repository.entity.PetPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetPhotoRepository extends JpaRepository<PetPhoto, Long> {
    List<PetPhoto> findByPetId(Long petId);
    void deleteByPetId(Long petId);
}

