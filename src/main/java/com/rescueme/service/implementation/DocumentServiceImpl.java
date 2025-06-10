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
            log.warn("Attempt to upload with invalid parameters: shelterId={}, documentType={}, file={}",
                    shelterId, documentType, file != null ? file.getOriginalFilename() : "null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid parameters for document upload");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("The file size exceeds the maximum allowed: {} bytes", file.getSize());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The file size exceeds the maximum allowed (2MB)");
        }

        String contentType = file.getContentType();
        if (!isValidContentType(contentType)) {
            log.warn("Invalid file type: {}", contentType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file type. Only PDF, JPEG, and PNG files are accepted");
        }

        try {
            log.debug("Uploading document: {} for shelter: {}", documentType, shelterId);
            log.debug("Original file name: {}", file.getOriginalFilename());
            log.debug("Content type: {}", file.getContentType());
            log.debug("File size: {} bytes", file.getSize());


            byte[] fileContent = file.getBytes();

            Optional<Document> existingDoc = documentRepository.findByShelterIdAndType(shelterId, documentType);

            Document document;
            if (existingDoc.isPresent()) {
                log.debug("Updating existing document");
                document = existingDoc.get();
            } else {
                log.debug("Creating new document");
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
            log.debug("Document saved with ID: {}", saved.getId());

        } catch (IOException e) {
            log.error("Error reading file content: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error processing document: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while uploading document: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error while processing document");
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
                    "Invalid document type: " + documentType);
        }

        if (!documentRepository.findByShelterIdAndType(shelterId, documentType).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Document not found for shelter: " + shelterId + " and type: " + documentType);
        }

        documentRepository.deleteByShelterIdAndType(shelterId, documentType);
        log.debug("Document deleted for shelter: {} and type: {}", shelterId, documentType);
    }


    private Document findDocument(Long shelterId, String documentType) {
        return documentRepository.findByShelterIdAndType(shelterId, documentType)
                .orElseThrow(() -> {
                    log.warn("Document not found for shelter: {} and type: {}", shelterId, documentType);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Document not found for shelter: " + shelterId + " and type: " + documentType);
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