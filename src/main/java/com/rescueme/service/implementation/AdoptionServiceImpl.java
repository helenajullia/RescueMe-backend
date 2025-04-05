package com.rescueme.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rescueme.repository.AdoptionRequestRepository;
import com.rescueme.repository.PetRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.dto.AdoptionRequestDTO;
import com.rescueme.repository.dto.AdoptionResponseDTO;
import com.rescueme.repository.entity.*;
import com.rescueme.service.AdoptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionRequestRepository adoptionRequestRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public AdoptionRequest createAdoptionRequest(Map<String, Object> requestData) {
        // Your existing implementation
        // Extract data from request
        Long userId = Long.valueOf(requestData.get("userId").toString());
        Long petId = Long.valueOf(requestData.get("petId").toString());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Validate pet exists and is available for adoption
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with ID: " + petId));

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            throw new IllegalArgumentException("This pet is not available for adoption");
        }

        // Check if user already has a pending request for this pet
        if (adoptionRequestRepository.existsByUserIdAndPetId(userId, petId)) {
            throw new IllegalArgumentException("You already have a pending adoption request for this pet");
        }

        // Create the adoption request
        AdoptionRequest adoptionRequest = new AdoptionRequest();
        adoptionRequest.setUser(user);
        adoptionRequest.setPet(pet);
        adoptionRequest.setRequestDetails((Map<String, Object>) requestData.get("requestDetails"));
        adoptionRequest.setStatus(AdoptionRequestStatus.PENDING);
        adoptionRequest.setRequestDate(LocalDateTime.now());

        // Update pet status to PENDING
        pet.setStatus(PetStatus.PENDING);
        petRepository.save(pet);

        // Save and return the adoption request
        return adoptionRequestRepository.save(adoptionRequest);
    }

    // Other original methods implementation...
    @Override
    public AdoptionRequest getAdoptionRequestById(String requestId) {
        return adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Adoption request not found with ID: " + requestId));
    }

    @Override
    public List<AdoptionRequest> getUserAdoptionRequests(Long userId) {
        return adoptionRequestRepository.findByUserId(userId);
    }

    @Override
    public List<AdoptionRequest> getShelterAdoptionRequests(Long shelterId) {
        return adoptionRequestRepository.findByShelterIdOrderByRequestDateDesc(shelterId);
    }

    @Override
    public List<AdoptionRequest> getPetAdoptionRequests(Long petId) {
        return adoptionRequestRepository.findByPetId(petId);
    }

    @Override
    public AdoptionRequest updateAdoptionRequestStatus(String requestId, AdoptionRequestStatus status, String notes) {
        AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Adoption request not found with ID: " + requestId));

        // Only allow updates to PENDING requests
        if (adoptionRequest.getStatus() != AdoptionRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Cannot update request that is not in PENDING status. Current status: " + adoptionRequest.getStatus());
        }

        // Update request status and notes
        adoptionRequest.setStatus(status);
        adoptionRequest.setNotes(notes);
        adoptionRequest.setResponseDate(LocalDateTime.now());

        // Update pet status based on the request status
        Pet pet = adoptionRequest.getPet();

        switch (status) {
            case APPROVED:
                pet.setStatus(PetStatus.ADOPTED);

                // Reject all other pending requests for this pet
                List<AdoptionRequest> otherRequests = adoptionRequestRepository.findByPetId(pet.getId());
                for (AdoptionRequest otherRequest : otherRequests) {
                    if (otherRequest.getId().equals(requestId)) {
                        continue; // Skip the current request
                    }

                    if (otherRequest.getStatus() == AdoptionRequestStatus.PENDING) {
                        otherRequest.setStatus(AdoptionRequestStatus.REJECTED);
                        otherRequest.setNotes("Another adopter has been selected for this pet.");
                        otherRequest.setResponseDate(LocalDateTime.now());
                        adoptionRequestRepository.save(otherRequest);
                    }
                }
                break;

            case REJECTED:
                // If request is rejected, check if there are other pending requests
                // If no other pending requests, set pet back to AVAILABLE
                List<AdoptionRequest> pendingRequests = adoptionRequestRepository.findByPetIdAndStatus(pet.getId(), AdoptionRequestStatus.PENDING);
                if (pendingRequests.isEmpty() || pendingRequests.size() == 1) { // Only current request
                    pet.setStatus(PetStatus.AVAILABLE);
                }
                break;

            case CANCELED:
                // Same logic as REJECTED
                pendingRequests = adoptionRequestRepository.findByPetIdAndStatus(pet.getId(), AdoptionRequestStatus.PENDING);
                if (pendingRequests.isEmpty() || pendingRequests.size() == 1) {
                    pet.setStatus(PetStatus.AVAILABLE);
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported status update: " + status);
        }

        petRepository.save(pet);
        return adoptionRequestRepository.save(adoptionRequest);
    }

    @Override
    public void cancelAdoptionRequest(String requestId) {
        AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Adoption request not found with ID: " + requestId));

        // Only allow cancellation of PENDING requests
        if (adoptionRequest.getStatus() != AdoptionRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Cannot cancel request that is not in PENDING status. Current status: " + adoptionRequest.getStatus());
        }

        // Update request status
        adoptionRequest.setStatus(AdoptionRequestStatus.CANCELED);
        adoptionRequest.setResponseDate(LocalDateTime.now());

        // Check if there are other pending requests for this pet
        Pet pet = adoptionRequest.getPet();
        List<AdoptionRequest> pendingRequests = adoptionRequestRepository.findByPetIdAndStatus(pet.getId(), AdoptionRequestStatus.PENDING);

        // If this was the only pending request, set pet back to AVAILABLE
        if (pendingRequests.isEmpty()) {
            pet.setStatus(PetStatus.AVAILABLE);
            petRepository.save(pet);
        }

        adoptionRequestRepository.save(adoptionRequest);
    }

    // New DTO-based methods implementation
    @Override
    @Transactional
    public AdoptionResponseDTO createAdoptionRequestDTO(AdoptionRequestDTO requestDTO) {
        // Validate user exists
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        // Validate pet exists and is available for adoption
        Pet pet = petRepository.findById(requestDTO.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with ID: " + requestDTO.getPetId()));

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            throw new IllegalArgumentException("This pet is not available for adoption");
        }

        // Check if user already has a pending request for this pet
        if (adoptionRequestRepository.existsByUserIdAndPetId(requestDTO.getUserId(), requestDTO.getPetId())) {
            throw new IllegalArgumentException("You already have a pending adoption request for this pet");
        }

        // Create the adoption request
        AdoptionRequest adoptionRequest = new AdoptionRequest();
        adoptionRequest.setUser(user);
        adoptionRequest.setPet(pet);
        adoptionRequest.setRequestDetails(requestDTO.getRequestDetails());
        adoptionRequest.setStatus(AdoptionRequestStatus.PENDING);
        adoptionRequest.setRequestDate(LocalDateTime.now());

        // Update pet status to PENDING
        pet.setStatus(PetStatus.PENDING);
        petRepository.save(pet);

        // Save and get the adoption request
        AdoptionRequest savedRequest = adoptionRequestRepository.save(adoptionRequest);

        // Convert to DTO and return
        return convertToDTO(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public AdoptionResponseDTO getAdoptionRequestDTOById(String requestId) {
        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Adoption request not found with ID: " + requestId));
        return convertToDTO(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionResponseDTO> getUserAdoptionRequestDTOs(Long userId) {
        List<AdoptionRequest> requests = adoptionRequestRepository.findByUserId(userId);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionResponseDTO> getShelterAdoptionRequestDTOs(Long shelterId) {
        List<AdoptionRequest> requests = adoptionRequestRepository.findByShelterIdOrderByRequestDateDesc(shelterId);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionResponseDTO> getPetAdoptionRequestDTOs(Long petId) {
        List<AdoptionRequest> requests = adoptionRequestRepository.findByPetId(petId);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdoptionResponseDTO updateAdoptionRequestStatusDTO(String requestId, AdoptionRequestStatus status, String notes) {
        AdoptionRequest updatedRequest = updateAdoptionRequestStatus(requestId, status, notes);
        return convertToDTO(updatedRequest);
    }

    // Helper method to convert entity to DTO
    private AdoptionResponseDTO convertToDTO(AdoptionRequest adoptionRequest) {
        AdoptionResponseDTO dto = new AdoptionResponseDTO();
        dto.setId(adoptionRequest.getId());
        dto.setUserId(adoptionRequest.getUser().getId());
        dto.setUserName(adoptionRequest.getUser().getUsername());
        dto.setPetId(adoptionRequest.getPet().getId());
        dto.setPetName(adoptionRequest.getPet().getName());
        dto.setRequestDetails(adoptionRequest.getRequestDetails());
        dto.setStatus(adoptionRequest.getStatus());
        dto.setRequestDate(adoptionRequest.getRequestDate());
        dto.setResponseDate(adoptionRequest.getResponseDate());
        dto.setNotes(adoptionRequest.getNotes());
        return dto;
    }
}