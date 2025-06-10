package com.rescueme.service.implementation;

import com.rescueme.repository.DonationRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.dto.DonationRequestDTO;
import com.rescueme.repository.dto.DonationResponseDTO;
import com.rescueme.repository.entity.Donation;
import com.rescueme.repository.entity.User;
import com.rescueme.service.DonationService;
import com.stripe.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public DonationServiceImpl(DonationRepository donationRepository, UserRepository userRepository) {
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createDonationIntent(DonationRequestDTO requestDTO) {
        User shelter = userRepository.findById(requestDTO.getShelterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        if (!requestDTO.isAnonymous() && requestDTO.getUserId() != null) {
            userRepository.findById(requestDTO.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        if (requestDTO.getAmount() == null || requestDTO.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid donation amount");
        }

        try {
            Stripe.apiKey = stripeApiKey;

            long amountInCents = Math.round(requestDTO.getAmount() * 100);

            PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                    .setCurrency("ron")
                    .setAmount(amountInCents)
                    .setDescription("Donation towards: " + shelter.getUsername())
                    .putMetadata("shelter_id", shelter.getId().toString())
                    .putMetadata("donor_id", requestDTO.getUserId() != null ? requestDTO.getUserId().toString() : "anonymous")
                    .putMetadata("is_anonymous", String.valueOf(requestDTO.isAnonymous()))
                    .build();

            PaymentIntent intent = PaymentIntent.create(createParams);

            Donation donation = new Donation();
            donation.setShelterId(requestDTO.getShelterId());
            donation.setDonorId(requestDTO.isAnonymous() ? null : requestDTO.getUserId());
            donation.setAmount(requestDTO.getAmount());
            donation.setMessage(requestDTO.getMessage());
//            donation.setAnonymous(requestDTO.isAnonymous());
            donation.setTransactionId(intent.getId());
            donation.setPaymentStatus(Donation.PaymentStatus.PENDING);
            donation.setDonationDate(LocalDateTime.now());

            donationRepository.save(donation);

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("donationId", donation.getId());

            return response;
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error creating payment intent: " + e.getMessage());
        }
    }

    
    @Override
    @Transactional
    public void processPaymentWebhook(String payload, String stripeSignature) {
        try {
            Event event = Webhook.constructEvent(payload, stripeSignature, webhookSecret);

            String eventType = event.getType();
            System.out.println("Received Stripe event: " + eventType);

            String finalTransactionId = null;

            if ("payment_intent.succeeded".equals(eventType)) {
                JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject()
                        .getAsJsonObject("data")
                        .getAsJsonObject("object");

                if (jsonObject != null && jsonObject.has("id")) {
                    finalTransactionId = jsonObject.get("id").getAsString();
                    System.out.println("Extracted payment intent ID: " + finalTransactionId);
                }
            } else if ("charge.succeeded".equals(eventType)) {
                JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject()
                        .getAsJsonObject("data")
                        .getAsJsonObject("object");

                if (jsonObject != null && jsonObject.has("payment_intent")) {
                    finalTransactionId = jsonObject.get("payment_intent").getAsString();
                    System.out.println("Extracted payment intent ID from charge: " + finalTransactionId);
                }
            }

            if (finalTransactionId != null) {
                System.out.println("Processing payment with transaction ID: " + finalTransactionId);

                final String transactionId = finalTransactionId;

                List<Donation> donations = donationRepository.findAll().stream()
                        .filter(d -> transactionId.equals(d.getTransactionId()))
                        .collect(Collectors.toList());

                if (!donations.isEmpty()) {
                    for (Donation donation : donations) {
                        donation.setPaymentStatus(Donation.PaymentStatus.COMPLETED);
                        donationRepository.save(donation);
                        System.out.println("Updated donation ID " + donation.getId() + " status to COMPLETED");
                    }
                } else {
                    System.out.println("No donation found with transaction ID: " + transactionId);
                    List<String> allIds = donationRepository.findAll().stream()
                            .map(Donation::getTransactionId)
                            .collect(Collectors.toList());
                    System.out.println("All transaction IDs in database: " + allIds);
                }
            } else {
                System.out.println("Could not extract payment intent ID from event");
            }
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public List<DonationResponseDTO> getDonationsForShelter(Long shelterId) {
        User shelter = userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        List<Donation> donations = donationRepository.findByShelterId(shelterId);

        return donations.stream().map(donation -> {
            String donorName = null;
            if (donation.getDonorId() != null) {
                donorName = userRepository.findById(donation.getDonorId())
                        .map(User::getUsername)
                        .orElse("Unknown");
            } else {
                donorName = "Anonymous";
            }

            return DonationResponseDTO.fromEntity(donation, shelter.getUsername(), donorName);
        }).collect(Collectors.toList());
    }

    @Override
    public List<DonationResponseDTO> getDonationsForUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Donation> donations = donationRepository.findByDonorId(userId);

        return donations.stream().map(donation -> {
            String shelterName = userRepository.findById(donation.getShelterId())
                    .map(User::getUsername)
                    .orElse("Unknown Shelter");

            return DonationResponseDTO.fromEntity(donation, shelterName, null);
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDonationStatistics(Long shelterId) {
        userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalDonations", donationRepository.countByShelterId(shelterId));

        Double totalAmount = donationRepository.getTotalDonationAmountForShelter(shelterId);
        stats.put("totalAmountRaised", totalAmount != null ? totalAmount : 0.0);

        List<DonationResponseDTO> recentDonations = donationRepository.findRecentDonationsForShelter(shelterId, 5)
                .stream()
                .map(donation -> {
                    String donorName = null;
                    if (donation.getDonorId() != null) {
                        donorName = userRepository.findById(donation.getDonorId())
                                .map(User::getUsername)
                                .orElse("Unknown");
                    }

                    String shelterName = userRepository.findById(donation.getShelterId())
                            .map(User::getUsername)
                            .orElse("Unknown Shelter");

                    return DonationResponseDTO.fromEntity(donation, shelterName, donorName);
                })
                .collect(Collectors.toList());

        stats.put("recentDonations", recentDonations);

        return stats;
    }

    @Override
    @Transactional
    public void deleteDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donation not found"));

        donationRepository.delete(donation);
    }
}