package com.rescueme.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShelterRegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String password;

    @NotBlank
    private String county;

    @NotBlank
    private String city;

    @NotBlank
    private String shelterType;
}
