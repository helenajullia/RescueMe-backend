package com.rescueme.service.implementation;

import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.ShelterStatus;
import com.rescueme.repository.entity.User;
import com.rescueme.service.DocumentService;
import com.rescueme.service.ShelterProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShelterProfileServiceImpl implements ShelterProfileService {

    private final UserRepository userRepository;
    private final DocumentService documentService; // Folosim serviciul specializat de documente

    @Override
    @Transactional
    public User saveShelterProfileDraft(Long shelterId, Map<String, Object> profileData) {
        User shelter = userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        updateShelterFromMap(shelter, profileData);

        // Set status to DRAFT
        shelter.setStatus(ShelterStatus.DRAFT);

        log.debug("Saving shelter profile draft for ID: {}", shelterId);
        return userRepository.save(shelter);
    }

    @Override
    @Transactional
    public User submitShelterProfile(Long shelterId, Map<String, Object> profileData) {
        User shelter = userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        updateShelterFromMap(shelter, profileData);

        // Validate required fields for submission
        validateProfileForSubmission(shelter);

        // Set status to PENDING_APPROVAL and record submission time
        shelter.setStatus(ShelterStatus.PENDING_APPROVAL);
        shelter.setSubmittedAt(LocalDateTime.now());

        log.debug("Submitting shelter profile for ID: {}", shelterId);
        return userRepository.save(shelter);
    }

    @Override
    @Transactional
    public void uploadDocument(Long shelterId, String documentType, MultipartFile file) {
        // Delegăm încărcarea documentului către serviciul specializat
        documentService.uploadDocument(shelterId, documentType, file);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getDocument(Long shelterId, String documentType) {
        // Delegăm obținerea documentului către serviciul specializat
        return documentService.getDocument(shelterId, documentType);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentContentType(Long shelterId, String documentType) {
        // Delegăm obținerea tipului de conținut către serviciul specializat
        return documentService.getDocumentContentType(shelterId, documentType);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Boolean> getDocumentStatus(Long shelterId) {
        // Delegăm verificarea statutului documentelor către serviciul specializat
        return documentService.getDocumentStatus(shelterId);
    }

    // Helper methods

    private void updateShelterFromMap(User shelter, Map<String, Object> profileData) {
        if (profileData.containsKey("username") && profileData.get("username") != null) {
            shelter.setUsername((String) profileData.get("username"));
        }

        if (profileData.containsKey("email") && profileData.get("email") != null) {
            shelter.setEmail((String) profileData.get("email"));
        }

        if (profileData.containsKey("phoneNumber") && profileData.get("phoneNumber") != null) {
            shelter.setPhoneNumber((String) profileData.get("phoneNumber"));
        }

        if (profileData.containsKey("shelterType") && profileData.get("shelterType") != null) {
            shelter.setShelterType((String) profileData.get("shelterType"));
        }

        if (profileData.containsKey("county") && profileData.get("county") != null) {
            shelter.setCounty((String) profileData.get("county"));
        }

        if (profileData.containsKey("city") && profileData.get("city") != null) {
            shelter.setCity((String) profileData.get("city"));
        }

        if (profileData.containsKey("fullAddress") && profileData.get("fullAddress") != null) {
            shelter.setFullAddress((String) profileData.get("fullAddress"));
        }

        if (profileData.containsKey("zipCode") && profileData.get("zipCode") != null) {
            shelter.setZipCode((String) profileData.get("zipCode"));
        }

        if (profileData.containsKey("yearFounded") && profileData.get("yearFounded") != null) {
            if (profileData.get("yearFounded") instanceof Integer) {
                shelter.setYearFounded((Integer) profileData.get("yearFounded"));
            } else if (profileData.get("yearFounded") instanceof String) {
                try {
                    shelter.setYearFounded(Integer.parseInt((String) profileData.get("yearFounded")));
                } catch (NumberFormatException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid year format");
                }
            }
        }

        if (profileData.containsKey("hoursOfOperation") && profileData.get("hoursOfOperation") != null) {
            shelter.setHoursOfOperation((String) profileData.get("hoursOfOperation"));
        }

        if (profileData.containsKey("mission") && profileData.get("mission") != null) {
            shelter.setMission((String) profileData.get("mission"));
        }
    }

    private void validateProfileForSubmission(User shelter) {
        StringBuilder errors = new StringBuilder();

        if (isNullOrEmpty(shelter.getUsername())) {
            errors.append("Username is required. ");
        }

        if (isNullOrEmpty(shelter.getEmail())) {
            errors.append("Email is required. ");
        }

        if (isNullOrEmpty(shelter.getPhoneNumber())) {
            errors.append("Phone number is required. ");
        }

        if (isNullOrEmpty(shelter.getShelterType())) {
            errors.append("Shelter type is required. ");
        }

        if (isNullOrEmpty(shelter.getCounty())) {
            errors.append("County is required. ");
        }

        if (isNullOrEmpty(shelter.getCity())) {
            errors.append("City is required. ");
        }

        if (isNullOrEmpty(shelter.getFullAddress())) {
            errors.append("Full address is required. ");
        }

        if (isNullOrEmpty(shelter.getZipCode())) {
            errors.append("ZIP/Postal code is required. ");
        }

        if (shelter.getYearFounded() == null) {
            errors.append("Year founded is required. ");
        } else if (shelter.getYearFounded() < 1900 || shelter.getYearFounded() > LocalDateTime.now().getYear()) {
            errors.append("Year founded must be between 1900 and current year. ");
        }

        if (isNullOrEmpty(shelter.getHoursOfOperation())) {
            errors.append("Hours of operation are required. ");
        }

        if (isNullOrEmpty(shelter.getMission())) {
            errors.append("Mission statement is required. ");
        } else if (shelter.getMission().length() < 50) {
            errors.append("Mission statement should be at least 50 characters long. ");
        }

        // Check if all required documents are uploaded
        Map<String, Boolean> documentStatus = getDocumentStatus(shelter.getId());
        if (!documentStatus.get("taxCertificate")) {
            errors.append("Tax certificate document is required. ");
        }
        if (!documentStatus.get("vetAuthorization")) {
            errors.append("Veterinary authorization document is required. ");
        }
        if (!documentStatus.get("vetContract")) {
            errors.append("Veterinarian contract document is required. ");
        }
        if (!documentStatus.get("idCard")) {
            errors.append("ID card document is required. ");
        }

        if (errors.length() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}