package com.rescueme.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @JsonIgnore
    private LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    private PetStatus status;

    @Column(name = "story", columnDefinition = "TEXT")
    private String story;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PetPhoto> photos;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User shelter;

}