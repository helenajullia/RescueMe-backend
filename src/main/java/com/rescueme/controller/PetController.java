package com.rescueme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rescueme.repository.dto.PetResponseDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetService;
import com.rescueme.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetResponseDTO> addPet(
            @RequestPart("petData") String petDataString,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestHeader("shelterId") Long shelterId
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Pet petData = objectMapper.readValue(petDataString, Pet.class);

            User shelter = userService.getUserById(shelterId);
            Pet savedPet = petService.addPet(petData, shelter, photos);

            return ResponseEntity.ok(PetResponseDTO.toDto(savedPet));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{shelterId}")
    public ResponseEntity<List<PetResponseDTO>> getPetsByShelterId(@PathVariable Long shelterId) {
        List<PetResponseDTO> pets = petService.getPetsByShelterId(shelterId);
        return ResponseEntity.ok(pets);
    }


    @DeleteMapping("/{shelterId}/delete/{petId}")
    public ResponseEntity<String> deletePet(@PathVariable Long shelterId, @PathVariable Long petId) {
        if (petService.deletePetByShelterId(shelterId, petId)) {
            return ResponseEntity.ok("Pet deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this pet or pet not found");
        }
    }


}
