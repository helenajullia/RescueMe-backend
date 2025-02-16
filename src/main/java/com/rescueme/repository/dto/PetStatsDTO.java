package com.rescueme.repository.dto;

public class PetStatsDTO {
    private int adopted;
    private int pending;
    private int available;

    public PetStatsDTO(int adopted, int pending, int available) { // Numele constructorului trebuie să fie identic cu numele clasei
        this.adopted = adopted;
        this.pending = pending;
        this.available = available;
    }

    public int getAdopted() {
        return adopted;
    }

    public int getPending() {
        return pending;
    }

    public int getAvailable() {
        return available;
    }
}
