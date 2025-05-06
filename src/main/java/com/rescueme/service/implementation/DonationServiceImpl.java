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
        // Verifică dacă shelter-ul există
        User shelter = userRepository.findById(requestDTO.getShelterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        // Verifică donatorul dacă nu este anonim
        if (!requestDTO.isAnonymous() && requestDTO.getUserId() != null) {
            userRepository.findById(requestDTO.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        // Verifică suma
        if (requestDTO.getAmount() == null || requestDTO.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid donation amount");
        }

        try {
            // Inițializează Stripe cu cheia API
            Stripe.apiKey = stripeApiKey;

            // Convertește suma în cenți (Stripe folosește cea mai mică unitate monetară)
            long amountInCents = Math.round(requestDTO.getAmount() * 100);

            // Creează un PaymentIntent cu suma și moneda
            PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                    .setCurrency("ron") // Codul monedei românești
                    .setAmount(amountInCents)
                    .setDescription("Donație către " + shelter.getUsername())
                    .putMetadata("shelter_id", shelter.getId().toString())
                    .putMetadata("donor_id", requestDTO.getUserId() != null ? requestDTO.getUserId().toString() : "anonymous")
                    .putMetadata("is_anonymous", String.valueOf(requestDTO.isAnonymous()))
                    .build();

            // Creează intenția de plată
            PaymentIntent intent = PaymentIntent.create(createParams);

            // Creează înregistrarea donației în stare de așteptare
            Donation donation = new Donation();
            donation.setShelterId(requestDTO.getShelterId());
            donation.setDonorId(requestDTO.isAnonymous() ? null : requestDTO.getUserId());
            donation.setAmount(requestDTO.getAmount());
            donation.setMessage(requestDTO.getMessage());
            donation.setAnonymous(requestDTO.isAnonymous());
            donation.setTransactionId(intent.getId());
            donation.setPaymentStatus(Donation.PaymentStatus.PENDING);
            donation.setDonationDate(LocalDateTime.now());

            // Salvează donația
            donationRepository.save(donation);

            // Returnează client secret și ID-ul donației
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
            // Verify the webhook signature
            Event event = Webhook.constructEvent(payload, stripeSignature, webhookSecret);

            // Log the event type for debugging
            String eventType = event.getType();
            System.out.println("Received Stripe event: " + eventType);

            // Extract and define transaction ID
            String finalTransactionId = null;

            if ("payment_intent.succeeded".equals(eventType)) {
                // Get the data object directly from the JSON structure
                JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject()
                        .getAsJsonObject("data")
                        .getAsJsonObject("object");

                if (jsonObject != null && jsonObject.has("id")) {
                    finalTransactionId = jsonObject.get("id").getAsString();
                    System.out.println("Extracted payment intent ID: " + finalTransactionId);
                }
            } else if ("charge.succeeded".equals(eventType)) {
                // Get the data object directly from the JSON structure
                JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject()
                        .getAsJsonObject("data")
                        .getAsJsonObject("object");

                if (jsonObject != null && jsonObject.has("payment_intent")) {
                    finalTransactionId = jsonObject.get("payment_intent").getAsString();
                    System.out.println("Extracted payment intent ID from charge: " + finalTransactionId);
                }
            }

            // Process the transaction if we have an ID
            if (finalTransactionId != null) {
                System.out.println("Processing payment with transaction ID: " + finalTransactionId);

                // Use a local final variable for the lambda
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
                    // Debug: print all transaction IDs in database
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

    private void updateDonationStatus(String transactionId, Donation.PaymentStatus status) {
        // Găsește donația după ID-ul tranzacției
        Donation donation = donationRepository.findAll().stream()
                .filter(d -> transactionId.equals(d.getTransactionId()))
                .findFirst()
                .orElse(null);

        // Actualizează statusul dacă donația a fost găsită
        if (donation != null) {
            donation.setPaymentStatus(status);
            donationRepository.save(donation);
        }
    }

    @Override
    public List<DonationResponseDTO> getDonationsForShelter(Long shelterId) {
        // Verifică dacă shelter-ul există
        User shelter = userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        // Obține donațiile finalizate
        List<Donation> donations = donationRepository.findByShelterId(shelterId);

        // Convertește în DTO-uri cu numele shelter-ului și donatorului
        return donations.stream().map(donation -> {
            String donorName = null;
            if (!donation.isAnonymous() && donation.getDonorId() != null) {
                donorName = userRepository.findById(donation.getDonorId())
                        .map(User::getUsername)
                        .orElse("Unknown");
            } else {
                donorName = "Anonymous"; // Explicitly set anonymous name
            }

            return DonationResponseDTO.fromEntity(donation, shelter.getUsername(), donorName);
        }).collect(Collectors.toList());
    }

    @Override
    public List<DonationResponseDTO> getDonationsForUser(Long userId) {
        // Verifică dacă utilizatorul există
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Obține donațiile făcute de acest utilizator
        List<Donation> donations = donationRepository.findByDonorId(userId);

        // Convertește în DTO-uri cu numele shelter-urilor
        return donations.stream().map(donation -> {
            String shelterName = userRepository.findById(donation.getShelterId())
                    .map(User::getUsername)
                    .orElse("Unknown Shelter");

            return DonationResponseDTO.fromEntity(donation, shelterName, null);
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDonationStatistics(Long shelterId) {
        // Verifică shelter-ul
        userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        Map<String, Object> stats = new HashMap<>();

        // Număr total de donații
        stats.put("totalDonations", donationRepository.countByShelterId(shelterId));

        // Suma totală strânsă
        Double totalAmount = donationRepository.getTotalDonationAmountForShelter(shelterId);
        stats.put("totalAmountRaised", totalAmount != null ? totalAmount : 0.0);

        // Donații recente (ultimele 5)
        List<DonationResponseDTO> recentDonations = donationRepository.findRecentDonationsForShelter(shelterId, 5)
                .stream()
                .map(donation -> {
                    String donorName = null;
                    if (!donation.isAnonymous() && donation.getDonorId() != null) {
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
}