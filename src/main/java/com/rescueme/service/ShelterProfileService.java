package com.rescueme.service;

import com.rescueme.repository.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ShelterProfileService {
    User saveShelterProfileDraft(Long shelterId, Map<String, Object> profileData);

    @Transactional
    User submitShelterProfile(Long shelterId, Map<String, Object> profileData);

    @Transactional
    void uploadDocument(Long shelterId, String documentType, MultipartFile file);

    @Transactional
    byte[] getDocument(Long shelterId, String documentType);

    String getDocumentContentType(Long shelterId, String documentType);

    Map<String, Boolean> getDocumentStatus(Long shelterId);
}