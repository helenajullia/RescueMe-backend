package com.rescueme.service.implementation;

import com.rescueme.repository.PetRepository;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetService;
import org.springframework.stereotype.Service;
import com.rescueme.repository.dto.PetResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;

    public PetServiceImpl(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public Pet addPet(Pet pet, User shelter) {
        pet.setShelter(shelter);
        System.out.println("Description before saving: " + pet.getStory());
        return petRepository.save(pet);
    }


//    @Override
//    public List<Pet> getPetsByShelter(User shelter) {
//        return petRepository.findByShelterId(shelter.getId());
//    }

    @Override
    public Pet getPetById(Long id) {
        return petRepository.findById(id).orElseThrow(() -> new RuntimeException("Pet not found"));
    }

    @Override
    public Pet updatePet(Long id, Pet petDetails) {return null;}

    @Override
    public void deletePet(Long id) {}

    @Override
    public List<PetResponseDTO> getPetsByShelterId(Long shelterId) {
        return petRepository.findByShelterId(shelterId).stream()
                .map(pet -> new PetResponseDTO(
                        pet.getId(),
                        pet.getName(),
                        pet.getSpecies(),
                        pet.getBreed(),
                        pet.getSex(),
                        pet.getAge(),
                        pet.getSize(),
                        pet.getHealthStatus(),
                        pet.isVaccinated(),
                        pet.isNeutered(),
                        pet.isUrgentAdoptionNeeded(),
                        pet.getTimeSpentInShelter(),
                        pet.getStatus().name(),
                        pet.getStory(),
                        pet.getPhotos(),
                        pet.getCreatedAt(),
                        pet.getShelter().getId(),
                        pet.getShelter().getUsername()
                ))
                .collect(Collectors.toList());
    }
}
