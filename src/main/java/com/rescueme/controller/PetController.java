package com.rescueme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rescueme.repository.dto.PetResponseDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetService;
import com.rescueme.service.PhotoService;
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

    private final PhotoService photoService;

    public PetController(PetService petService, UserService userService, PhotoService photoService) {
        this.petService = petService;
        this.userService = userService;
        this.photoService=photoService;

    }

//    @PostMapping("/add")
//    public ResponseEntity<Pet> addPet(@RequestBody Pet pet, @RequestHeader("shelterId") Long shelterId) {
//        User shelter = userService.getUserById(shelterId);
//        Pet savedPet = petService.addPet(pet, shelter);
//        return ResponseEntity.ok(savedPet);
//    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Pet> addPet(
            @RequestPart("petData") String petDataString,
            @RequestPart("photos") List<MultipartFile> photos,
            @RequestHeader("shelterId") Long shelterId
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Pet petData = objectMapper.readValue(petDataString, Pet.class);

            User shelter = userService.getUserById(shelterId);
            petData.setShelter(shelter);

            Pet savedPet = petService.addPet(petData, shelter);

            List<String> photoUrls = photoService.processPhotos(photos, savedPet.getId());
            savedPet.setPhotoUrls(photoUrls);

            petService.updatePet(savedPet.getId(), photoUrls);

//            System.out.println("Pet data: " + petData);
//            System.out.println("Photos received: " + photos.size());
//            System.out.println("Photo URLs: " + photoUrls);

            return ResponseEntity.ok(savedPet);
        } catch (Exception e) {
            e.printStackTrace();
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
