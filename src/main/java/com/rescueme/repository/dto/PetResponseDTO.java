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

}
