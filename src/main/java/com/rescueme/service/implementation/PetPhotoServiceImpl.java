package com.rescueme.service.implementation;

import com.rescueme.repository.PetPhotoRepository;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetPhoto;
import com.rescueme.service.PetPhotoService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class PetPhotoServiceImpl implements PetPhotoService {

    private final PetPhotoRepository petPhotoRepository;


    public PetPhotoServiceImpl(PetPhotoRepository petPhotoRepository) {
        this.petPhotoRepository = petPhotoRepository;
    }

    @Override
    public void addPhotosToPet(Pet pet, List<MultipartFile> photos) {
        if (photos != null && !photos.isEmpty()) {
            List<PetPhoto> petPhotos = photos.stream().map(photo -> {
                try {
                    PetPhoto petPhoto = new PetPhoto();
                    petPhoto.setPhotoData(photo.getBytes());
                    petPhoto.setPet(pet);
                    return petPhoto;
                } catch (IOException e) {
                    throw new RuntimeException("Error processing photo", e);
                }
            }).collect(Collectors.toList());

            petPhotoRepository.saveAll(petPhotos);
        }
    }


    @Override
    public void saveAllPhotos(List<PetPhoto> petPhotos) {
        petPhotoRepository.saveAll(petPhotos);
    }


    @Override
    public List<PetPhoto> getPhotosByPetId(Long petId) {
        return petPhotoRepository.findByPetId(petId);
    }

    @Transactional
    @Override
    public void deletePhotosByPetId(Long petId) {
        petPhotoRepository.deleteByPetId(petId);
    }

    @Override
    public void deletePhotoById(Long photoId) {
        petPhotoRepository.deleteById(photoId);
    }


}
