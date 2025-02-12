package com.rescueme.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
}
