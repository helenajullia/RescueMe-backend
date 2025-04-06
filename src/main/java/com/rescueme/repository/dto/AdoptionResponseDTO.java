package com.rescueme.repository.dto;

import com.rescueme.repository.entity.AdoptionRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionResponseDTO {
    private String id;
    private Long userId;
    private String userName;
    private PetDTO pet;
    private String petName;
    private Map<String, Object> requestDetails;
    private AdoptionRequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime responseDate;
    private String notes;
}