package com.rescueme.repository;

import com.rescueme.repository.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByShelterId(Long shelterId);

    List<Donation> findByDonorId(Long donorId);

    long countByShelterId(Long shelterId);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.shelterId = :shelterId AND d.paymentStatus = 'COMPLETED'")
    Double getTotalDonationAmountForShelter(@Param("shelterId") Long shelterId);

    @Query(value = "SELECT * FROM donations d WHERE d.shelter_id = :shelterId AND d.payment_status = 'COMPLETED' ORDER BY d.donation_date DESC LIMIT :limit", nativeQuery = true)
    List<Donation> findRecentDonationsForShelter(@Param("shelterId") Long shelterId, @Param("limit") int limit);
}