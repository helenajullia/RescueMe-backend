package com.rescueme.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rescueme.repository.dto.PetResponseDTO;
import com.rescueme.repository.dto.PetStatsDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetStatus;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetService;
import com.rescueme.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;
    private final UserService userService;

    public PetController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;

    }

    /**
     * Returns a list of all pets in the system
     */
    @GetMapping("/all")
    public ResponseEntity<List<PetResponseDTO>> getAllPets() {
        List<PetResponseDTO> allPets = petService.getAllPets();
        return ResponseEntity.ok(allPets);
    }

    /**
     * Adds a new pet to the shelter's profile
     * Accepts multipart form data with pet info and optional photos
     */
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_SHELTER')")
    public ResponseEntity<PetResponseDTO> addPet(
            @RequestPart("petData") String petDataString,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestHeader("Id") Long shelterId
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Pet petData = objectMapper.readValue(petDataString, Pet.class);


            User shelter = userService.getUserById(shelterId);
            Pet savedPet = petService.addPet(petData, shelter, photos);

            return ResponseEntity.ok(PetResponseDTO.toDto(savedPet));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns all pets belonging to a specific shelter
     */
    @GetMapping("/{shelterId}")
    public ResponseEntity<List<PetResponseDTO>> getPetsByShelterId(@PathVariable Long shelterId) {
        List<PetResponseDTO> pets = petService.getPetsByShelterId(shelterId);
        return ResponseEntity.ok(pets);
    }

    /**
     * Deletes a pet by its ID and shelter ID
     * Only allowed if the pet belongs to the shelter
     */
    @DeleteMapping("/{shelterId}/delete/{petId}")
    @PreAuthorize("hasRole('ROLE_SHELTER')")
    public ResponseEntity<String> deletePet(@PathVariable Long shelterId, @PathVariable Long petId) {
        if (petService.deletePetByShelterId(shelterId, petId)) {
            return ResponseEntity.ok("Pet deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this pet or pet not found");
        }
    }

    /**
     * Updates a pet by ID
     * Accepts partial updates, new photos, and photo IDs to delete
     */
    @PatchMapping(value = "/update/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePet(
            @PathVariable Long petId,
            @RequestPart(value = "petData", required = false) String petDataString,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestPart(value = "photoIdsToDelete", required = false) String photoIdsToDeleteJson,
            @RequestHeader("Id") Long Id
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Pet updatedPetData = petDataString != null ? objectMapper.readValue(petDataString, Pet.class) : new Pet();

            List<Long> photoIdsToDelete = photoIdsToDeleteJson != null ?
                    objectMapper.readValue(photoIdsToDeleteJson, new TypeReference<List<Long>>() {}) :
                    List.of();

            Pet updatedPet = petService.updatePet(petId, updatedPetData, Id, photos, photoIdsToDelete);

            return ResponseEntity.ok(PetResponseDTO.toDto(updatedPet));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating pet: " + e.getMessage());
        }
    }

    /**
     * Returns pet statistics for a specific shelter
     */
    @GetMapping("/stats/{shelterId}")
    public ResponseEntity<PetStatsDTO> getPetStatsByShelter(@PathVariable Long shelterId) {
        PetStatsDTO stats = petService.getPetStatsByShelter(shelterId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Returns a list of all unique breeds from all pets
     */
    @GetMapping("/breeds")
    public ResponseEntity<List<String>> getBreeds() {
        List<String> breeds = petService.getAllBreeds();
        return ResponseEntity.ok(breeds);
    }

    /**
     * Returns all breeds for a specific species (Dog or Cat)
     */
    @GetMapping("/breedsBySpecies")
    public ResponseEntity<List<String>> getBreeds(@RequestParam String species) {
        List<String> breeds = petService.getBreedsBySpecies(species);
        return ResponseEntity.ok(breeds);
    }

    /**
     * Returns the number of pets owned by a specific shelter
     */
    @GetMapping("/count/{shelterId}")
    public ResponseEntity<Long> getPetCountByShelter(@PathVariable Long shelterId) {
        long petCount = petService.countPetsByShelter(shelterId);
        return ResponseEntity.ok(petCount);
    }

    /**
     * Returns all pets that are currently available for adoption
     */
    @GetMapping("/available")
    public ResponseEntity<List<PetResponseDTO>> getAllAvailablePets() {
        List<PetResponseDTO> pets = petService.getPetsByStatus(PetStatus.AVAILABLE);
        return ResponseEntity.ok(pets);
    }
}
