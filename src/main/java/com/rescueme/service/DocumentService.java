package com.rescueme.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface DocumentService {
    /**
     * Încarcă un document pentru un adăpost
     */
    void uploadDocument(Long shelterId, String documentType, MultipartFile file);

    /**
     * Obține conținutul unui document
     */
    byte[] getDocument(Long shelterId, String documentType);

    /**
     * Obține tipul de conținut al unui document
     */
    String getDocumentContentType(Long shelterId, String documentType);

    /**
     * Obține statutul documentelor pentru un adăpost
     * Returnează un map cu tipurile de documente și boolean care indică dacă există
     */
    Map<String, Boolean> getDocumentStatus(Long shelterId);

    /**
     * Șterge un document
     */
    void deleteDocument(Long shelterId, String documentType);
}