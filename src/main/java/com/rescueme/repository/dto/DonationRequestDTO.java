package com.rescueme.repository.dto;

import lombok.Data;

// DTO pentru cererea de donație
@Data
public class DonationRequestDTO {
    private Long shelterId;
    private Long userId; // Opțional, poate fi null pentru donații anonime
    private Double amount;
    private String message;
    private boolean isAnonymous;
}
