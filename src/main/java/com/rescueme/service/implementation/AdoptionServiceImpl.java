package com.rescueme.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rescueme.repository.AdoptionRequestRepository;
import com.rescueme.repository.PetRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.dto.AdoptionRequestDTO;
import com.rescueme.repository.dto.AdoptionResponseDTO;
import com.rescueme.repository.dto.PetDTO;
import com.rescueme.repository.entity.*;
import com.rescueme.service.AdoptionService;
import com.rescueme.service.NotificationService;
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
    private final NotificationService notificationService;



    @Override
    @Transactional
    public AdoptionRequest createAdoptionRequest(Map<String, Object> requestData) {
        Long userId = Long.valueOf(requestData.get("userId").toString());
        Long petId = Long.valueOf(requestData.get("petId").toString());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with ID: " + petId));

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            throw new IllegalArgumentException("This pet is not available for adoption");
        }

        if (adoptionRequestRepository.existsByUserIdAndPetId(userId, petId)) {
            throw new IllegalArgumentException("You already have a pending adoption request for this pet");
        }

        AdoptionRequest adoptionRequest = new AdoptionRequest();
        adoptionRequest.setUser(user);
        adoptionRequest.setPet(pet);
        adoptionRequest.setRequestDetails((Map<String, Object>) requestData.get("requestDetails"));
        adoptionRequest.setStatus(AdoptionRequestStatus.PENDING);
        adoptionRequest.setRequestDate(LocalDateTime.now());

        return adoptionRequestRepository.save(adoptionRequest);
    }

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

        if (adoptionRequest.getStatus() != AdoptionRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Cannot update request that is not in PENDING status. Current status: " + adoptionRequest.getStatus());
        }

        adoptionRequest.setStatus(status);
        adoptionRequest.setNotes(notes);
        adoptionRequest.setResponseDate(LocalDateTime.now());

        Pet pet = adoptionRequest.getPet();

        switch (status) {
            case APPROVED:
                pet.setStatus(PetStatus.PENDING);

                List<AdoptionRequest> otherRequests = adoptionRequestRepository.findByPetId(pet.getId());
                for (AdoptionRequest otherRequest : otherRequests) {
                    if (otherRequest.getId().equals(requestId)) {
                        continue;
                    }

                    if (otherRequest.getStatus() == AdoptionRequestStatus.PENDING) {
                        otherRequest.setStatus(AdoptionRequestStatus.REJECTED);
                        otherRequest.setNotes("Another adopter has been selected for this pet.");
                        otherRequest.setResponseDate(LocalDateTime.now());
                        adoptionRequestRepository.save(otherRequest);

                        notificationService.sendNotificationToAdopter(
                                otherRequest.getUser().getId(),
                                pet.getName(),
                                false
                        );
                    }
                }
                break;

            case REJECTED:
                break;

            case CANCELED:
                break;

            default:
                throw new IllegalArgumentException("Unsupported status update: " + status);
        }

        petRepository.save(pet);

        boolean approved = status == AdoptionRequestStatus.APPROVED;
        notificationService.sendNotificationToAdopter(adoptionRequest.getUser().getId(), pet.getName(), approved);

        return adoptionRequestRepository.save(adoptionRequest);
    }

    @Override
    @Transactional
    public AdoptionRequest completeAdoption(String requestId) {
        AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Adoption request not found with ID: " + requestId));

        if (adoptionRequest.getStatus() != AdoptionRequestStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Cannot complete adoption that is not in APPROVED status. Current status: " + adoptionRequest.getStatus());
        }

        adoptionRequest.setStatus(AdoptionRequestStatus.COMPLETED);

        Pet pet = adoptionRequest.getPet();
        pet.setStatus(PetStatus.ADOPTED);

        petRepository.save(pet);
        return adoptionRequestRepository.save(adoptionRequest);
    }


    @Override
    public void cancelAdoptionRequest(String requestId) {
        AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Adoption request not found with ID: " + requestId));

        if (adoptionRequest.getStatus() != AdoptionRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Cannot cancel request that is not in PENDING status. Current status: " + adoptionRequest.getStatus());
        }

        adoptionRequest.setStatus(AdoptionRequestStatus.CANCELED);
        adoptionRequest.setResponseDate(LocalDateTime.now());

        Pet pet = adoptionRequest.getPet();
        List<AdoptionRequest> pendingRequests = adoptionRequestRepository.findByPetIdAndStatus(pet.getId(), AdoptionRequestStatus.PENDING);

        if (pendingRequests.isEmpty()) {
            pet.setStatus(PetStatus.AVAILABLE);
            petRepository.save(pet);
        }

        adoptionRequestRepository.save(adoptionRequest);
    }

    @Override
    @Transactional
    public AdoptionResponseDTO createAdoptionRequestDTO(AdoptionRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        Pet pet = petRepository.findById(requestDTO.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with ID: " + requestDTO.getPetId()));

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            throw new IllegalArgumentException("This pet is not available for adoption");
        }

        if (adoptionRequestRepository.existsByUserIdAndPetId(requestDTO.getUserId(), requestDTO.getPetId())) {
            throw new IllegalArgumentException("You already have a pending adoption request for this pet");
        }

        AdoptionRequest adoptionRequest = new AdoptionRequest();
        adoptionRequest.setUser(user);
        adoptionRequest.setPet(pet);
        adoptionRequest.setRequestDetails(requestDTO.getRequestDetails());
        adoptionRequest.setStatus(AdoptionRequestStatus.PENDING);
        adoptionRequest.setRequestDate(LocalDateTime.now());


        AdoptionRequest savedRequest = adoptionRequestRepository.save(adoptionRequest);

        notificationService.sendNotificationToShelter(pet.getShelter().getId(), pet.getName());

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


    private AdoptionResponseDTO convertToDTO(AdoptionRequest adoptionRequest) {
        AdoptionResponseDTO dto = new AdoptionResponseDTO();
        dto.setId(adoptionRequest.getId());
        dto.setUserId(adoptionRequest.getUser().getId());
        dto.setUserName(adoptionRequest.getUser().getUsername());
        dto.setPet(new PetDTO(adoptionRequest.getPet()));
        dto.setPetName(adoptionRequest.getPet().getName());
        dto.setRequestDetails(adoptionRequest.getRequestDetails());
        dto.setStatus(adoptionRequest.getStatus());
        dto.setRequestDate(adoptionRequest.getRequestDate());
        dto.setResponseDate(adoptionRequest.getResponseDate());
        dto.setNotes(adoptionRequest.getNotes());
        return dto;
    }
}