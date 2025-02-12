package com.rescueme.repository.dto;

import com.rescueme.repository.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class PetResponseDTO {
        private Long id;
        private String name;
        private String species;
        private String breed;
        private String sex;
        private double age;
        private String size;
        private String healthStatus;
        private boolean vaccinated;
        private boolean neutered;
        private boolean urgentAdoptionNeeded;
        private String timeSpentInShelter;
        private String status;
        private String story;
        private List<String> photoUrls;
        private LocalDate createdAt;
        private Long shelterId;
        private String shelterUsername;

        public static PetResponseDTO toDto(Pet pet) {
                return new PetResponseDTO(
                        pet.getId(),
                        pet.getName(),
                        pet.getSpecies(),
                        pet.getBreed(),
                        pet.getSex(),
                        pet.getAge(),
                        pet.getSize(),
                        pet.getHealthStatus(),
                        pet.isVaccinated(),
                        pet.isNeutered(),
                        pet.isUrgentAdoptionNeeded(),
                        pet.getTimeSpentInShelter(),
                        pet.getStatus().name(),
                        pet.getStory(),
                        pet.getPhotos() != null
                                ? pet.getPhotos().stream()
                                .map(photo -> "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(photo.getPhotoData()))
                                .collect(Collectors.toList())
                                : null,
                        pet.getCreatedAt(),
                        pet.getShelter().getId(),
                        pet.getShelter().getUsername()
                );
        }
}
