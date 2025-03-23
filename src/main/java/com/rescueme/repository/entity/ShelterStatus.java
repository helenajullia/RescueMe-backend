package com.rescueme.repository.entity;

public enum ShelterStatus {
    NEW,        // Just registered
    DRAFT,      // Profile saved as draft
    PENDING_APPROVAL,  // Profile submitted and awaiting admin approval
    APPROVED,   // Profile approved by admin
    REJECTED    // Profile rejected by admin
}
