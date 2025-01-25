package com.rescueme.repository.dto;


import com.rescueme.repository.entity.Role;
import lombok.Data;
import java.util.List;

@Data
public class UserResponseDTO {
    private String username;
    private String fullName;
    private String email;
    private Role role;
}
