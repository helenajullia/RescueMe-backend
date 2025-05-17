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
    Optional<Document> findByShelterIdAndType(Long shelterId, String type);
    void deleteByShelterIdAndType(Long shelterId, String type);
    @Query("SELECT d.type FROM Document d WHERE d.shelterId = :shelterId")
    List<String> findDocumentTypesByShelter(@Param("shelterId") Long shelterId);
}