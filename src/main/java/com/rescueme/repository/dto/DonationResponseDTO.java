package com.rescueme.repository.dto;

import com.rescueme.repository.entity.Donation;
import lombok.Data;

import java.time.LocalDateTime;

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
    private String donorName;
    private String message;

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

        if (!donation.isAnonymous() && donorName != null) {
            dto.setDonorName(donorName);
        } else {
            dto.setDonorName(null);
        }

        dto.setMessage(donation.getMessage());
        return dto;
    }
}