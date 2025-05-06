package com.rescueme.controller;

import com.rescueme.repository.dto.DonationRequestDTO;
import com.rescueme.repository.dto.DonationResponseDTO;
import com.rescueme.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    /**
     * Endpoint pentru crearea intenției de donație
     */
    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, Object>> createDonationIntent(@RequestBody DonationRequestDTO requestDTO) {
        Map<String, Object> result = donationService.createDonationIntent(requestDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint pentru primirea webhook-urilor de la Stripe
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        donationService.processPaymentWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed");
    }

    /**
     * Endpoint pentru obținerea donațiilor unui shelter
     */
    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<List<DonationResponseDTO>> getDonationsForShelter(@PathVariable Long shelterId) {
        List<DonationResponseDTO> donations = donationService.getDonationsForShelter(shelterId);
        return ResponseEntity.ok(donations);
    }

    /**
     * Endpoint pentru obținerea donațiilor unui utilizator
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DonationResponseDTO>> getDonationsForUser(@PathVariable Long userId) {
        List<DonationResponseDTO> donations = donationService.getDonationsForUser(userId);
        return ResponseEntity.ok(donations);
    }

    /**
     * Endpoint pentru obținerea statisticilor despre donații
     */
    @GetMapping("/statistics/{shelterId}")
    public ResponseEntity<Map<String, Object>> getDonationStatistics(@PathVariable Long shelterId) {
        Map<String, Object> statistics = donationService.getDonationStatistics(shelterId);
        return ResponseEntity.ok(statistics);
    }
}