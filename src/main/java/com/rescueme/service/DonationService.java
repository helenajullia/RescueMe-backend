package com.rescueme.service;

import com.rescueme.repository.dto.DonationRequestDTO;
import com.rescueme.repository.dto.DonationResponseDTO;

import java.util.List;
import java.util.Map;

public interface DonationService {
    /**
     * Creează o intenție de donație și returnează informațiile necesare
     * pentru procesarea plății pe partea de client
     */
    Map<String, Object> createDonationIntent(DonationRequestDTO requestDTO);

    /**
     * Procesează webhook-urile de la Stripe pentru a actualiza statusul donațiilor
     */
    void processPaymentWebhook(String payload, String stripeSignature);

    /**
     * Obține lista de donații pentru un shelter
     */
    List<DonationResponseDTO> getDonationsForShelter(Long shelterId);

    /**
     * Obține lista de donații făcute de un utilizator
     */
    List<DonationResponseDTO> getDonationsForUser(Long userId);

    /**
     * Obține statistici despre donații pentru un shelter
     */
    Map<String, Object> getDonationStatistics(Long shelterId);
}