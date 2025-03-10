package com.rescueme.repository.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetPhoto;
import com.rescueme.repository.entity.PetStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class PetDTO {
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
    private String timeSpentInShelter="0";
    private PetStatus status;

    private String story;

//    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<PetPhoto> photos = new ArrayList<>();


    // Constructor
    public PetDTO(Pet pet) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.species = pet.getSpecies();
        this.breed = pet.getBreed();
        this.sex = pet.getSex();
        this.age = pet.getAge();
        this.size = pet.getSize();
        this.healthStatus = pet.getHealthStatus();
        this.neutered = pet.isNeutered();
        this.vaccinated = pet.isVaccinated();
        this.urgentAdoptionNeeded = pet.isUrgentAdoptionNeeded();
        this.timeSpentInShelter= pet.getTimeSpentInShelter();
        this.status=pet.getStatus();
        this.story=pet.getStory();
    }

    // Getters & Setters
}

