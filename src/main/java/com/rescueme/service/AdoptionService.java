package com.rescueme.service;

import com.rescueme.repository.dto.AdoptionRequestDTO;
import com.rescueme.repository.dto.AdoptionResponseDTO;
import com.rescueme.repository.entity.AdoptionRequest;
import com.rescueme.repository.entity.AdoptionRequestStatus;

import java.util.List;
import java.util.Map;

public interface AdoptionService {

    AdoptionRequest createAdoptionRequest(Map<String, Object> requestData);
    AdoptionRequest getAdoptionRequestById(String requestId);
    List<AdoptionRequest> getUserAdoptionRequests(Long userId);
    List<AdoptionRequest> getShelterAdoptionRequests(Long shelterId);
    List<AdoptionRequest> getPetAdoptionRequests(Long petId);
    AdoptionRequest updateAdoptionRequestStatus(String requestId, AdoptionRequestStatus status, String notes);
    void cancelAdoptionRequest(String requestId);

    // New DTO-based methods
    AdoptionResponseDTO createAdoptionRequestDTO(AdoptionRequestDTO requestDTO);
    AdoptionResponseDTO getAdoptionRequestDTOById(String requestId);
    List<AdoptionResponseDTO> getUserAdoptionRequestDTOs(Long userId);
    List<AdoptionResponseDTO> getShelterAdoptionRequestDTOs(Long shelterId);
    List<AdoptionResponseDTO> getPetAdoptionRequestDTOs(Long petId);
    AdoptionResponseDTO updateAdoptionRequestStatusDTO(String requestId, AdoptionRequestStatus status, String notes);
}