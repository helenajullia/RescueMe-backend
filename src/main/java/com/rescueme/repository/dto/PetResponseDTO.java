package com.rescueme.repository.dto;

import com.rescueme.repository.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class PetResponseDTO {
        private Long id;
        private String name;
        private String species;
        private String breed;
        private String sex;
        private int age;
        private String size;
        private String healthStatus;
        private boolean vaccinated;
        private boolean neutered;
        private boolean urgentAdoptionNeeded;
        private String timeSpentInShelter;
        private String status;
        private String story;
        private List<String> photos;
        private LocalDate createdAt;
        private Long shelterId;
        private String shelterUsername;

        // Constructor that accepts a Pet entity
//        public PetResponseDTO(Pet pet) {
//                this.id = pet.getId();
//                this.name = pet.getName();
//                this.species = pet.getSpecies();
//                this.breed = pet.getBreed();
//                this.sex = pet.getSex();
//                this.age = pet.getAge();
//                this.size = pet.getSize();
//                this.healthStatus = pet.getHealthStatus();
//                this.vaccinated = pet.isVaccinated();
//                this.neutered = pet.isNeutered();
//                this.urgentAdoptionNeeded = pet.isUrgentAdoptionNeeded();
//                this.timeSpentInShelter = pet.getTimeSpentInShelter();
//                // Handle null status here
//                this.status = pet.getStatus() != null ? pet.getStatus().name() : "UNKNOWN";
//                this.story = pet.getStory();
//                this.photos = pet.getPhotos();
//                this.createdAt = pet.getCreatedAt();
//                this.shelterId = pet.getShelter().getId();
//                this.shelterUsername = pet.getShelter().getUsername();
//        }
}
