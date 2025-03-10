package com.rescueme.controller;

import com.rescueme.repository.dto.PetPhotoDTO;
import com.rescueme.repository.entity.PetPhoto;
import com.rescueme.service.PetPhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/pet-photos")
public class PetPhotoController {

    private final PetPhotoService petPhotoService;

    public PetPhotoController(PetPhotoService petPhotoService) {
        this.petPhotoService = petPhotoService;
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<PetPhotoDTO> getPhoto(@PathVariable Long photoId) {
        PetPhoto photo = petPhotoService.getPhotoById(photoId);
        return ResponseEntity.ok(PetPhotoDTO.toDto(photo));
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<PetPhotoDTO>> getPhotosByPetId(@PathVariable Long petId) {
        List<PetPhoto> photos = petPhotoService.getPhotosByPetId(petId);
        return ResponseEntity.ok(PetPhotoDTO.toDtoList(photos));
    }

    @GetMapping("/ids/pet/{petId}")
    public ResponseEntity<List<Long>> getPhotoIdsByPetId(@PathVariable Long petId) {
        List<Long> photoIds = petPhotoService.getPhotosByPetId(petId).stream()
                .map(PetPhoto::getId)
                .toList();
        return ResponseEntity.ok(photoIds);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        petPhotoService.deletePhotoById(photoId);
        return ResponseEntity.noContent().build();
    }
}