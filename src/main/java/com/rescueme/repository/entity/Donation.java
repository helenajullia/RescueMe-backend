package com.rescueme.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id")
    private Long donorId; // Poate fi null pentru donații anonime

    @Column(name = "shelter_id", nullable = false)
    private Long shelterId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency = "RON"; // Implicit moneda românească

    @Column(name = "donation_date", nullable = false)
    private LocalDateTime donationDate;

    @Column(name = "transaction_id")
    private String transactionId; // ID de la procesatorul de plăți

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "is_anonymous")
    private boolean isAnonymous;

    @Column(length = 500)
    private String message;

    // Pre-persist pentru a seta automat data donației
    @PrePersist
    public void prePersist() {
        if (donationDate == null) {
            donationDate = LocalDateTime.now();
        }
    }

    // Enum pentru statusul plății
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}