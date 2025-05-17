package com.rescueme.repository.dto;

import lombok.Data;

@Data
public class DonationRequestDTO {
    private Long shelterId;
    private Long userId;
    private Double amount;
    private String message;
    private boolean isAnonymous;
}
