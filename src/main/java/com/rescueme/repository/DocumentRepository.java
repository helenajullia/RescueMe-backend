package com.rescueme.repository;

import com.rescueme.repository.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Găsește un document specific pentru un adăpost și un tip de document
    Optional<Document> findByShelterIdAndType(Long shelterId, String type);

    // Găsește toate documentele unui adăpost
    List<Document> findByShelterId(Long shelterId);

    // Găsește documentele unui adăpost pentru anumite tipuri
    List<Document> findByShelterIdAndTypeIn(Long shelterId, List<String> types);

    // Șterge un document specific pentru un adăpost și un tip
    void deleteByShelterIdAndType(Long shelterId, String type);

    // Obține toate tipurile de documente pentru un adăpost (doar numele tipurilor)
    @Query("SELECT d.type FROM Document d WHERE d.shelterId = :shelterId")
    List<String> findDocumentTypesByShelter(@Param("shelterId") Long shelterId);
}