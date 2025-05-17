package com.rescueme.repository.dto;

import lombok.Data;

@Data
public class PetStatsDTO {
    private int adopted;
    private int pending;
    private int available;
    private long total;
    private int urgent;
    private Long shelterId;

    public PetStatsDTO(int adopted, int pending, int available, Long shelterId, long total, int urgent) {
        this.adopted = adopted;
        this.pending = pending;
        this.available = available;
        this.shelterId = shelterId;
        this.total = total;
        this.urgent = urgent;
    }
}
