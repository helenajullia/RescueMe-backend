package com.rescueme.repository.dto;

import com.rescueme.repository.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String county;
    private String city;
    private String shelterType;
    private String biography;

    private byte[] profilePicture;
    private List<PetDTO> pets;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.county = user.getCounty();
        this.city = user.getCity();
        this.shelterType=user.getShelterType();
        this.biography = user.getBiography();
        this.profilePicture= user.getProfilePicture();
        this.pets = user.getPets().stream().map(PetDTO::new).toList();
    }
}
