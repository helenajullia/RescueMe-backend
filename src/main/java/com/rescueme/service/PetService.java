package com.rescueme.service;

import com.rescueme.repository.dto.PetResponseDTO;
import com.rescueme.repository.dto.PetStatsDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PetService {

    Pet addPet(Pet pet, User shelter, List<MultipartFile> photos);

//    List<Pet> getPetsByShelter(User shelter);

    Pet getPetById(Long id);

    Pet updatePet(Long petId, Pet updatedPetData, Long shelterId, List<MultipartFile> newPhotos, List<Long> photoIdsToDelete);

    public boolean deletePetByShelterId(Long shelterId, Long petId);

    public List<PetResponseDTO> getPetsByShelterId(Long shelterId);

    PetStatsDTO getPetStats();

    PetStatsDTO getPetStatsByShelter(Long shelterId);
    List<String> getAllBreeds();
    List<String> getBreedsBySpecies(String species);

    long countPetsByShelter(Long shelterId);
}
