package com.rescueme.service.implementation;

import com.rescueme.repository.PetRepository;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetStatus;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetPhotoService;
import com.rescueme.service.PetService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.rescueme.repository.dto.PetResponseDTO;
import org.springframework.web.multipart.MultipartFile;
import com.rescueme.repository.dto.PetStatsDTO;

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
    public Pet updatePet(Long petId, Pet updatedPetData, Long shelterId, List<MultipartFile> newPhotos, List<Long> photoIdsToDelete) {
        Optional<Pet> optionalPet = petRepository.findById(petId);

        if (optionalPet.isEmpty()) {
            throw new EntityNotFoundException("Pet not found with ID: " + petId);
        }

        Pet existingPet = optionalPet.get();

        // 🛑 Verificăm permisiunea
        if (!existingPet.getShelter().getId().equals(shelterId)) {
            throw new SecurityException("You do not have permission to update this pet.");
        }

        // 🔄 Actualizăm doar câmpurile trimise
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

        // 🖼️ Ștergem doar pozele specificate
        if (!photoIdsToDelete.isEmpty()) {
            for (Long photoId : photoIdsToDelete) {
                petPhotoService.deletePhotoById(photoId);
            }
        }

        // 📸 Adăugăm pozele noi dacă există
        if (newPhotos != null && !newPhotos.isEmpty()) {
            petPhotoService.addPhotosToPet(existingPet, newPhotos);
        }

        return petRepository.save(existingPet);
    }




    @Override
    public PetStatsDTO getPetStats() {
        int adoptedCount = petRepository.countByStatus(PetStatus.ADOPTED);
        int pendingCount = petRepository.countByStatus(PetStatus.PENDING);
        int availableCount = petRepository.countByStatus(PetStatus.AVAILABLE);

        return new PetStatsDTO(adoptedCount, pendingCount, availableCount);
    }


    @Override
    public PetStatsDTO getPetStatsByShelter(Long shelterId) {
        int adoptedCount = petRepository.countByShelterIdAndStatus(shelterId, PetStatus.ADOPTED);
        int pendingCount = petRepository.countByShelterIdAndStatus(shelterId, PetStatus.PENDING);
        int availableCount = petRepository.countByShelterIdAndStatus(shelterId, PetStatus.AVAILABLE);

        return new PetStatsDTO(adoptedCount, pendingCount, availableCount, shelterId);
    }

}
