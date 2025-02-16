package com.rescueme.repository.dto;

import lombok.Data;

@Data
public class PetStatsDTO {
    private int adopted;
    private int pending;
    private int available;
    private Long shelterId;

    public PetStatsDTO(int adopted, int pending, int available) {
        this.adopted = adopted;
        this.pending = pending;
        this.available = available;
    }

    public PetStatsDTO(int adopted, int pending, int available, Long shelterId) {
        this.adopted = adopted;
        this.pending = pending;
        this.available = available;
        this.shelterId = shelterId;
    }

}
