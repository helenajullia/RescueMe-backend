package com.rescueme.repository.dto;

import com.rescueme.repository.entity.Donation;
import lombok.Data;

import java.time.LocalDateTime;

// DTO pentru răspunsul cu informații despre donație
@Data
public class DonationResponseDTO {
    private Long id;
    private Long shelterId;
    private String shelterName;
    private Double amount;
    private String currency;
    private LocalDateTime donationDate;
    private String paymentStatus;
    private boolean isAnonymous;
    private String donorName; // Inclus doar dacă nu este anonim
    private String message;

    // Metodă statică pentru a converti entitatea în DTO
    public static DonationResponseDTO fromEntity(Donation donation, String shelterName, String donorName) {
        DonationResponseDTO dto = new DonationResponseDTO();
        dto.setId(donation.getId());
        dto.setShelterId(donation.getShelterId());
        dto.setShelterName(shelterName);
        dto.setAmount(donation.getAmount());
        dto.setCurrency(donation.getCurrency());
        dto.setDonationDate(donation.getDonationDate());
        dto.setPaymentStatus(donation.getPaymentStatus().name());
        dto.setAnonymous(donation.isAnonymous());

        // Set donorName to null when anonymous is true
        if (!donation.isAnonymous() && donorName != null) {
            dto.setDonorName(donorName);
        } else {
            dto.setDonorName(null); // Keep it null for anonymous donations
        }

        dto.setMessage(donation.getMessage());
        return dto;
    }
}