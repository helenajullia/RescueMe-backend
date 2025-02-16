package com.rescueme.repository.dto;

import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetPhoto;
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
        private List<PetPhotoDTO> photos;
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
                        PetPhotoDTO.toDtoList(pet.getPhotos()),
                        pet.getCreatedAt(),
                        pet.getShelter().getId(),
                        pet.getShelter().getUsername()
                );
        }

        @Data
        @AllArgsConstructor
        public static class PhotoDTO {
                private Long id;
                private String url;
        }
}
