package com.rescueme.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "username", nullable = false, unique = true)
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    private String username;


    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "county")
    private String county;

    @Column(name = "city")
    private String city;

    @Column(name = "shelter_type")
    private String shelterType;

    @Lob
    @JdbcType(VarbinaryJdbcType.class)
    @Column(name = "profile_picture", columnDefinition = "BYTEA")
    private byte[] profilePicture;

    @Column(name = "biography", length = 500)
    private String biography;

    @Column(name = "full_address", length = 255)
    private String fullAddress;

    @Column(name = "zip_code", length = 10)
    private String zipCode;
}
