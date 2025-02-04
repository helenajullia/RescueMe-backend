package com.rescueme.controller;

import com.rescueme.repository.dto.PetResponseDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.User;
import com.rescueme.service.PetService;
import com.rescueme.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add")
    public ResponseEntity<Pet> addPet(@RequestBody Pet pet, @RequestHeader("shelterId") Long shelterId) {
        User shelter = userService.getUserById(shelterId);

        Pet savedPet = petService.addPet(pet, shelter);
        return ResponseEntity.ok(savedPet);
    }

    @GetMapping("/{shelterId}")
    public ResponseEntity<List<PetResponseDTO>> getPetsByShelterId(@PathVariable Long shelterId) {
        List<PetResponseDTO> pets = petService.getPetsByShelterId(shelterId);
        return ResponseEntity.ok(pets);
    }



}
