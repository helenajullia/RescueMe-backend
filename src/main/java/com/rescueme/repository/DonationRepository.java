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
    // Găsește donații după shelter
    List<Donation> findByShelterId(Long shelterId);

    // Găsește donații după donator
    List<Donation> findByDonorId(Long donorId);

    // Găsește donații într-un interval de timp
    List<Donation> findByDonationDateBetween(LocalDateTime start, LocalDateTime end);

    // Numără total donații finalizate pentru un shelter
    long countByShelterId(Long shelterId);

    // Însumează valoarea totală a donațiilor pentru un shelter
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.shelterId = :shelterId AND d.paymentStatus = 'COMPLETED'")
    Double getTotalDonationAmountForShelter(@Param("shelterId") Long shelterId);

    // Găsește donații recente pentru un shelter
    @Query(value = "SELECT * FROM donations d WHERE d.shelter_id = :shelterId AND d.payment_status = 'COMPLETED' ORDER BY d.donation_date DESC LIMIT :limit", nativeQuery = true)
    List<Donation> findRecentDonationsForShelter(@Param("shelterId") Long shelterId, @Param("limit") int limit);
}