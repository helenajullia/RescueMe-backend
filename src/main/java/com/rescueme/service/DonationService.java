package com.rescueme.service;

import com.rescueme.repository.dto.DonationRequestDTO;
import com.rescueme.repository.dto.DonationResponseDTO;

import java.util.List;
import java.util.Map;

public interface DonationService {
    Map<String, Object> createDonationIntent(DonationRequestDTO requestDTO);

    void processPaymentWebhook(String payload, String stripeSignature);

    List<DonationResponseDTO> getDonationsForShelter(Long shelterId);

    List<DonationResponseDTO> getDonationsForUser(Long userId);

    Map<String, Object> getDonationStatistics(Long shelterId);
    void deleteDonation(Long donationId);
}