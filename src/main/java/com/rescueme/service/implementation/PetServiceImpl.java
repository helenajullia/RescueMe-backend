package com.rescueme.service.implementation;

import com.rescueme.repository.PetPhotoRepository;
import com.rescueme.repository.PetRepository;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetPhoto;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetPhotoService;
import com.rescueme.service.PetService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import com.rescueme.repository.dto.PetResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final PetPhotoService petPhotoService;

    public PetServiceImpl(PetRepository petRepository, PetPhotoService petPhotoService) {
        this.petRepository = petRepository;
        this.petPhotoService = petPhotoService;
    }

    @Override
    public Pet addPet(Pet pet, User shelter, List<MultipartFile> photos) {
        pet.setShelter(shelter);
        Pet savedPet = petRepository.save(pet);
        petPhotoService.addPhotosToPet(savedPet, photos);
        return savedPet;
    }


    @Override
    public Pet getPetById(Long id) {
        return petRepository.findById(id).orElseThrow(() -> new RuntimeException("Pet not found"));
    }


    @Override
    public boolean deletePetByShelterId(Long shelterId, Long petId) {
        Optional<Pet> optionalPet = petRepository.findById(petId);

        if (optionalPet.isPresent()) {
            Pet pet = optionalPet.get();
            if (pet.getShelter().getId().equals(shelterId)) {
                petRepository.deleteById(petId);
                return true;
            }
        }
        return false;
    }


    @Override
    public List<PetResponseDTO> getPetsByShelterId(Long shelterId) {
        return petRepository.findByShelterId(shelterId).stream()
                .map(PetResponseDTO::toDto)
                .collect(Collectors.toList());
    }

}
