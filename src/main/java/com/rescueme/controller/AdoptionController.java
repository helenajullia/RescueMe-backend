package com.rescueme.controller;

import com.rescueme.repository.dto.AdoptionRequestDTO;
import com.rescueme.repository.dto.AdoptionResponseDTO;
import com.rescueme.repository.entity.AdoptionRequest;
import com.rescueme.repository.entity.AdoptionRequestStatus;
import com.rescueme.service.AdoptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

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

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<AdoptionResponseDTO> getAdoptionRequest(@PathVariable String requestId) {
        try {
            AdoptionResponseDTO response = adoptionService.getAdoptionRequestDTOById(requestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Adoption request not found with ID: " + requestId);
        }
    }

    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<AdoptionResponseDTO>> getUserAdoptionRequests(@PathVariable Long userId) {
        List<AdoptionResponseDTO> requests = adoptionService.getUserAdoptionRequestDTOs(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/shelter/{shelterId}")
    public ResponseEntity<List<AdoptionResponseDTO>> getShelterAdoptionRequests(@PathVariable Long shelterId) {
        List<AdoptionResponseDTO> requests = adoptionService.getShelterAdoptionRequestDTOs(shelterId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/pet/{petId}")
    public ResponseEntity<List<AdoptionResponseDTO>> getPetAdoptionRequests(@PathVariable Long petId) {
        List<AdoptionResponseDTO> requests = adoptionService.getPetAdoptionRequestDTOs(petId);
        return ResponseEntity.ok(requests);
    }

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

    @DeleteMapping("/requests/{requestId}")
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
}