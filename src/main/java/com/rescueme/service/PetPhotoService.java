package com.rescueme.service;

import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PetPhotoService {

    void addPhotosToPet(Pet pet, List<MultipartFile> photos);
    void saveAllPhotos(List<PetPhoto> petPhotos);

    List<PetPhoto> getPhotosByPetId(Long petId);
    PetPhoto getPhotoById(Long photoId);

    void deletePhotosByPetId(Long petId);

    void deletePhotoById(Long photoId);
}
