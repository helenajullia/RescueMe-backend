package com.rescueme.service.implementation;

import com.rescueme.repository.DocumentRepository;
import com.rescueme.repository.entity.Document;
import com.rescueme.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private static final List<String> VALID_DOCUMENT_TYPES =
            Arrays.asList("taxCertificate", "vetAuthorization", "vetContract", "idCard");

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    @Transactional
    public void uploadDocument(Long shelterId, String documentType, MultipartFile file) {
        if (shelterId == null || !isValidDocumentType(documentType) || file == null || file.isEmpty()) {
            log.warn("Încercare de încărcare cu parametri invalizi: shelterId={}, documentType={}, file={}",
                    shelterId, documentType, file != null ? file.getOriginalFilename() : "null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parametri invalizi pentru încărcarea documentului");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Dimensiunea fișierului depășește maximul permis: {} bytes", file.getSize());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dimensiunea fișierului depășește maximul permis (2MB)");
        }

        String contentType = file.getContentType();
        if (!isValidContentType(contentType)) {
            log.warn("Tip de fișier invalid: {}", contentType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tip de fișier invalid. Sunt acceptate doar fișiere PDF, JPEG și PNG");
        }

        try {
            log.debug("Încărcare document: {} pentru adăpostul: {}", documentType, shelterId);
            log.debug("Numele original al fișierului: {}", file.getOriginalFilename());
            log.debug("Tip conținut: {}", file.getContentType());
            log.debug("Dimensiune fișier: {} bytes", file.getSize());

            byte[] fileContent = file.getBytes();

            Optional<Document> existingDoc = documentRepository.findByShelterIdAndType(shelterId, documentType);

            Document document;
            if (existingDoc.isPresent()) {
                log.debug("Actualizare document existent");
                document = existingDoc.get();
            } else {
                log.debug("Creare document nou");
                document = new Document();
                document.setShelterId(shelterId);
                document.setType(documentType);
                document.setCreatedAt(LocalDateTime.now());
            }

            document.setContent(fileContent);
            document.setFileName(file.getOriginalFilename());
            document.setContentType(file.getContentType());
            document.setUpdatedAt(LocalDateTime.now());
            document.setFileSize(file.getSize());

            Document saved = documentRepository.save(document);
            log.debug("Document salvat cu ID: {}", saved.getId());

        } catch (IOException e) {
            log.error("Eroare la citirea conținutului fișierului: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Eroare la procesarea documentului: " + e.getMessage());
        } catch (Exception e) {
            log.error("Eroare neașteptată la încărcarea documentului: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Eroare neașteptată la procesarea documentului");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getDocument(Long shelterId, String documentType) {
        Document document = findDocument(shelterId, documentType);
        return document.getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentContentType(Long shelterId, String documentType) {
        Document document = findDocument(shelterId, documentType);
        return document.getContentType();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Boolean> getDocumentStatus(Long shelterId) {
        Map<String, Boolean> status = new HashMap<>();

        for (String type : VALID_DOCUMENT_TYPES) {
            status.put(type, false);
        }

        List<String> documentTypes = documentRepository.findDocumentTypesByShelter(shelterId);
        for (String type : documentTypes) {
            status.put(type, true);
        }

        return status;
    }

    @Override
    @Transactional
    public void deleteDocument(Long shelterId, String documentType) {
        if (!isValidDocumentType(documentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tip de document invalid: " + documentType);
        }

        if (!documentRepository.findByShelterIdAndType(shelterId, documentType).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Documentul nu a fost găsit pentru adăpostul: " + shelterId + " și tipul: " + documentType);
        }

        documentRepository.deleteByShelterIdAndType(shelterId, documentType);
        log.debug("Document șters pentru adăpostul: {} și tipul: {}", shelterId, documentType);
    }


    private Document findDocument(Long shelterId, String documentType) {
        return documentRepository.findByShelterIdAndType(shelterId, documentType)
                .orElseThrow(() -> {
                    log.warn("Document negăsit pentru adăpostul: {} și tipul: {}", shelterId, documentType);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Document negăsit pentru adăpostul: " + shelterId + " și tipul: " + documentType);
                });
    }

    private boolean isValidDocumentType(String documentType) {
        return VALID_DOCUMENT_TYPES.contains(documentType);
    }

    private boolean isValidContentType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/png")
        );
    }
}