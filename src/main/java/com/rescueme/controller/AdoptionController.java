package com.rescueme.controller;

import com.rescueme.repository.dto.AdoptionRequestDTO;
import com.rescueme.repository.dto.AdoptionResponseDTO;
import com.rescueme.repository.entity.AdoptionRequest;
import com.rescueme.repository.entity.AdoptionRequestStatus;
import com.rescueme.service.AdoptionService;
import com.rescueme.utils.AdoptionMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    /**
     * Creates a new adoption request based on the data from the adopter
     */
    @PostMapping("/requests")
    public ResponseEntity<AdoptionResponseDTO> createAdoptionRequest(@RequestBody AdoptionRequestDTO requestDTO) {
        try {
            AdoptionResponseDTO response = adoptionService.createAdoptionRequestDTO(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating adoption request: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the adoption request with the given ID
     */
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<AdoptionResponseDTO> getAdoptionRequest(@PathVariable String requestId) {
        try {
            AdoptionResponseDTO response = adoptionService.getAdoptionRequestDTOById(requestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Adoption request not found with ID: " + requestId);
        }
    }

    /**
     * Returns all adoption requests made by a specific user
     */
    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<AdoptionResponseDTO>> getUserAdoptionRequests(@PathVariable Long userId) {
        List<AdoptionResponseDTO> requests = adoptionService.getUserAdoptionRequestDTOs(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Returns all adoption requests received by a specific shelter
     */
    @GetMapping("/requests/shelter/{shelterId}")
    public ResponseEntity<List<AdoptionResponseDTO>> getShelterAdoptionRequests(@PathVariable Long shelterId) {
        List<AdoptionResponseDTO> requests = adoptionService.getShelterAdoptionRequestDTOs(shelterId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Returns all adoption requests for a specific pet
     */
    @GetMapping("/requests/pet/{petId}")
    public ResponseEntity<List<AdoptionResponseDTO>> getPetAdoptionRequests(@PathVariable Long petId) {
        List<AdoptionResponseDTO> requests = adoptionService.getPetAdoptionRequestDTOs(petId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Updates the status and optional notes for an adoption request
     */
    @PatchMapping("/requests/{requestId}")
    public ResponseEntity<AdoptionResponseDTO> updateAdoptionRequestStatus(
            @PathVariable String requestId,
            @RequestBody Map<String, Object> updateData) {

        try {
            String status = (String) updateData.get("status");
            String notes = (String) updateData.get("notes");

            AdoptionRequestStatus requestStatus;
            try {
                requestStatus = AdoptionRequestStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
            }

            AdoptionResponseDTO updatedRequest = adoptionService.updateAdoptionRequestStatusDTO(requestId, requestStatus, notes);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating adoption request: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels an existing adoption request
     */
    @DeleteMapping("/requests/{requestId}")
    @PreAuthorize("hasRole('ROLE_ADOPTER')")
    public ResponseEntity<Void> cancelAdoptionRequest(@PathVariable String requestId) {
        try {
            adoptionService.cancelAdoptionRequest(requestId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error canceling adoption request: " + e.getMessage(), e);
        }
    }

    /**
     * Marks an adoption request as completed and finalizes the adoption
     */
    @PostMapping("/requests/{requestId}/complete")
    @PreAuthorize("hasRole('ROLE_SHELTER')")
    public ResponseEntity<AdoptionResponseDTO> completeAdoption(@PathVariable String requestId) {
        try {
            AdoptionRequest completedRequest = adoptionService.completeAdoption(requestId);
            AdoptionResponseDTO response = AdoptionMapperUtil.toDTO(completedRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error completing adoption: " + e.getMessage(), e);
        }
    }
}