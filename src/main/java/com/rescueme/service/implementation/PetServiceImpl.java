package com.rescueme.service.implementation;

import com.rescueme.repository.PetPhotoRepository;
import com.rescueme.repository.PetRepository;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetPhoto;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetPhotoService;
import com.rescueme.service.PetService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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

    @Override
    @Transactional
    public Pet updatePet(Long petId, Pet updatedPetData, Long shelterId, List<MultipartFile> newPhotos, boolean deleteExistingPhotos) {
        Optional<Pet> optionalPet = petRepository.findById(petId);

        if (optionalPet.isEmpty()) {
            throw new EntityNotFoundException("Pet not found with ID: " + petId);
        }

        Pet existingPet = optionalPet.get();

        // 🛑 Verificăm dacă utilizatorul are permisiunea de a modifica animalul
        if (!existingPet.getShelter().getId().equals(shelterId)) {
            throw new SecurityException("You do not have permission to update this pet.");
        }

        // 🔄 Actualizăm doar câmpurile care sunt trimise în request
        if (updatedPetData.getName() != null) existingPet.setName(updatedPetData.getName());
        if (updatedPetData.getSpecies() != null) existingPet.setSpecies(updatedPetData.getSpecies());
        if (updatedPetData.getBreed() != null) existingPet.setBreed(updatedPetData.getBreed());
        if (updatedPetData.getSex() != null) existingPet.setSex(updatedPetData.getSex());
        if (updatedPetData.getAge() != 0) existingPet.setAge(updatedPetData.getAge());
        if (updatedPetData.getSize() != null) existingPet.setSize(updatedPetData.getSize());
        if (updatedPetData.getHealthStatus() != null) existingPet.setHealthStatus(updatedPetData.getHealthStatus());
        existingPet.setVaccinated(updatedPetData.isVaccinated());
        existingPet.setNeutered(updatedPetData.isNeutered());
        existingPet.setUrgentAdoptionNeeded(updatedPetData.isUrgentAdoptionNeeded());
        if (updatedPetData.getTimeSpentInShelter() != null) existingPet.setTimeSpentInShelter(updatedPetData.getTimeSpentInShelter());
        if (updatedPetData.getStatus() != null) existingPet.setStatus(updatedPetData.getStatus());
        if (updatedPetData.getStory() != null) existingPet.setStory(updatedPetData.getStory());

        // 🖼️ Gestionăm pozele
        if (deleteExistingPhotos) {
            petPhotoService.deletePhotosByPetId(petId); // 🔥 Ștergem toate pozele dacă e cerut
        }

        if (newPhotos != null && !newPhotos.isEmpty()) {
            petPhotoService.addPhotosToPet(existingPet, newPhotos); // 📸 Adăugăm noile poze
        }

        return petRepository.save(existingPet);
    }

}
