package com.rescueme.service;

import com.rescueme.repository.dto.UserDTO;
import com.rescueme.repository.entity.User;
import com.rescueme.security.request.AdopterRegisterRequest;
import com.rescueme.security.request.ShelterRegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public interface UserService {

    void addAdopter(AdopterRegisterRequest registerRequest);

    Long addShelter(ShelterRegisterRequest registerRequest);

    User getUserById(Long userId);

    List<User> getAllUsers();

    void deleteUserById(Long userId);

    boolean emailExists(String email);

    boolean usernameExists(String username);

    User updateUser(Long userId, Map<String, Object> updates);

    void uploadProfilePicture(Long userId, MultipartFile file);

    byte[] getProfilePicture(Long userId);

    void deleteProfilePicture(Long userId);

    void changePassword(Long userId, String currentPassword, String newPassword);

    User getShelterById(Long shelterId);

    List<UserDTO> getAllShelters();
}
