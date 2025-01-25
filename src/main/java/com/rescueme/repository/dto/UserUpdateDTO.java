package com.rescueme.repository.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;
    private String fullName;
    private String email;
    private Long avatarId;
}