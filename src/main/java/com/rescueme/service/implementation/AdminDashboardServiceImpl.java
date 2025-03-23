package com.rescueme.service.implementation;

import com.rescueme.repository.PetRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.ShelterStatus;
import com.rescueme.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;

    @Override
    public Long getTotalSheltersCount() {
        return userRepository.countByRole(Role.SHELTER);
    }

    @Override
    public Long getPendingSheltersCount() {
        return userRepository.countByRoleAndStatus(Role.SHELTER, ShelterStatus.PENDING_APPROVAL);
    }

    @Override
    public Long getTotalUsersCount() {
        return userRepository.countByRole(Role.ADOPTER);
    }

    @Override
    public Long getTotalPetsCount() {
        return petRepository.count();
    }
}