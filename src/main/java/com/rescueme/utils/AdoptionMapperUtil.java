package com.rescueme.utils;

import com.rescueme.repository.dto.AdoptionResponseDTO;
import com.rescueme.repository.entity.AdoptionRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between AdoptionRequest entities and DTOs
 */
public class AdoptionMapperUtil {

    /**
     * Converts an AdoptionRequest entity to an AdoptionResponseDTO
     */
    public static AdoptionResponseDTO toDTO(AdoptionRequest adoptionRequest) {
        if (adoptionRequest == null) {
            return null;
        }

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

    /**
     * Converts a list of AdoptionRequest entities to a list of AdoptionResponseDTOs
     */
    public static List<AdoptionResponseDTO> toDTOList(List<AdoptionRequest> adoptionRequests) {
        if (adoptionRequests == null) {
            return null;
        }

        return adoptionRequests.stream()
                .map(AdoptionMapperUtil::toDTO)
                .collect(Collectors.toList());
    }
}