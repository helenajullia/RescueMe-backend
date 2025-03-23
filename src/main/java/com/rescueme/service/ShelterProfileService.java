package com.rescueme.service;

import com.rescueme.repository.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ShelterProfileService {

    /**
     * Save shelter profile data as a draft
     *
     * @param shelterId ID of the shelter
     * @param profileData Map containing profile data to save
     * @return Updated User entity
     */
    User saveShelterProfileDraft(Long shelterId, Map<String, Object> profileData);

    /**
     * Submit a shelter profile for approval
     *
     * @param shelterId ID of the shelter
     * @param profileData Map containing profile data to save
     * @return Updated User entity
     */
    @Transactional
    User submitShelterProfile(Long shelterId, Map<String, Object> profileData);

    /**
     * Upload a document for a shelter
     *
     * @param shelterId ID of the shelter
     * @param documentType Type of document (taxCertificate, vetAuthorization, vetContract, idCard)
     * @param file File to upload
     */
    @Transactional
    void uploadDocument(Long shelterId, String documentType, MultipartFile file);

    /**
     * Get a document for a shelter
     *
     * @param shelterId ID of the shelter
     * @param documentType Type of document
     * @return Document as byte array
     */
    @Transactional
    byte[] getDocument(Long shelterId, String documentType);

    /**
     * Get document content type
     *
     * @param shelterId ID of the shelter
     * @param documentType Type of document
     * @return Content type of the document
     */
    String getDocumentContentType(Long shelterId, String documentType);

    /**
     * Get document upload status for a shelter
     *
     * @param shelterId ID of the shelter
     * @return Map with document types as keys and boolean values indicating if they are uploaded
     */
    Map<String, Boolean> getDocumentStatus(Long shelterId);
}