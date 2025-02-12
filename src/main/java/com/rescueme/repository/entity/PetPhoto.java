package com.rescueme.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

@Entity
@Table(name = "pet_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @JdbcType(VarbinaryJdbcType.class)
    @Column(name = "photo_data", columnDefinition = "BYTEA")
    private byte[] photoData;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
}

