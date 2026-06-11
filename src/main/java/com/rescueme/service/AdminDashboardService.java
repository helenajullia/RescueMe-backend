package com.rescueme.service;

import com.rescueme.repository.entity.User;

import java.util.List;

public interface AdminDashboardService {
    Long getTotalSheltersCount();

    Long getPendingSheltersCount();

    Long getTotalUsersCount();

    Long getTotalPetsCount();

    List<User> getPendingShelters();

    List<User> getApprovedShelters();
}
