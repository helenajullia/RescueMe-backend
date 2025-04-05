package com.rescueme.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface DocumentService {

    void uploadDocument(Long shelterId, String documentType, MultipartFile file);
    byte[] getDocument(Long shelterId, String documentType);
    String getDocumentContentType(Long shelterId, String documentType);
    Map<String, Boolean> getDocumentStatus(Long shelterId);
    void deleteDocument(Long shelterId, String documentType);
}