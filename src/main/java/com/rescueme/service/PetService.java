package com.rescueme.service;

import com.rescueme.repository.dto.PetResponseDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.User;

import java.util.List;

public interface PetService {

    Pet addPet(Pet pet, User shelter);

//    List<Pet> getPetsByShelter(User shelter);

    Pet getPetById(Long id);

    Pet updatePet(Long id, Pet petDetails);

    void deletePet(Long id);

    public List<PetResponseDTO> getPetsByShelterId(Long shelterId);
}
