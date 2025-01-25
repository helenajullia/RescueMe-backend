package com.rescueme.service;

import com.rescueme.repository.entity.User;
import com.rescueme.security.request.AdopterRegisterRequest;
import com.rescueme.security.request.ShelterRegisterRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    void addAdopter(AdopterRegisterRequest registerRequest);

    void addShelter(ShelterRegisterRequest registerRequest);

    User getUserById(Long userId);

    List<User> getAllUsers();

    void deleteUserById(Long userId);


}
