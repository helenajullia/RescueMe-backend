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
     * Creates a Stripe donation payment intent based on the request data
     * Returns client secret and metadata used for frontend payment processing
     */
    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, Object>> createDonationIntent(@RequestBody DonationRequestDTO requestDTO) {
        Map<String, Object> result = donationService.createDonationIntent(requestDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * Handles Stripe webhook events to confirm or update donation status
     * Verifies the event signature and processes it accordingly
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        donationService.processPaymentWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed");
    }

    /**
     * Returns a list of all donations received by the given shelter
     */
    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<List<DonationResponseDTO>> getDonationsForShelter(@PathVariable Long shelterId) {
        List<DonationResponseDTO> donations = donationService.getDonationsForShelter(shelterId);
        return ResponseEntity.ok(donations);
    }

    /**
     * Returns a list of all donations made by a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DonationResponseDTO>> getDonationsForUser(@PathVariable Long userId) {
        List<DonationResponseDTO> donations = donationService.getDonationsForUser(userId);
        return ResponseEntity.ok(donations);
    }

    /**
     * Returns donation statistics for a given shelter
     */
    @GetMapping("/statistics/{shelterId}")
    public ResponseEntity<Map<String, Object>> getDonationStatistics(@PathVariable Long shelterId) {
        Map<String, Object> statistics = donationService.getDonationStatistics(shelterId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Deletes a donation by id
     */
    @DeleteMapping("/{donationId}")
    public ResponseEntity<Map<String, String>> deleteDonation(@PathVariable Long donationId) {
        donationService.deleteDonation(donationId);
        return ResponseEntity.ok(Map.of("message", "Donation deleted successfully"));
    }
}