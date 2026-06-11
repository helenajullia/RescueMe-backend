package com.rescueme.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionRequestDTO {
    private Long userId;
    private Long petId;
    private Map<String, Object> requestDetails;
}