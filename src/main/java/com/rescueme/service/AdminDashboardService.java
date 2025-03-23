package com.rescueme.service;

public interface AdminDashboardService {
    Long getTotalSheltersCount();

    Long getPendingSheltersCount();

    Long getTotalUsersCount();

    Long getTotalPetsCount();
}
