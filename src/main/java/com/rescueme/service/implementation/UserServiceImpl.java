package com.rescueme.service.implementation;

import com.rescueme.exception.UsernameAlreadyExistException;
import com.rescueme.repository.PetRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.dto.UserDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.User;
import com.rescueme.security.request.AdopterRegisterRequest;
import com.rescueme.security.request.ShelterRegisterRequest;
import com.rescueme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void addAdopter(AdopterRegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UsernameAlreadyExistException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.ADOPTER);
        user.setPhoneNumber(registerRequest.getPhoneNumber());


        user.setCounty(null);
        user.setCity(null);
        user.setShelterType(null);

        userRepository.save(user);
    }
    @Override
    public void addShelter(ShelterRegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UsernameAlreadyExistException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.SHELTER);
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setCounty(registerRequest.getCounty());
        user.setCity(registerRequest.getCity());
        user.setShelterType(registerRequest.getShelterType());

        userRepository.save(user);
    }


    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public User updateUser(Long userId, Map<String, Object> updates) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = optionalUser.get();

        updates.forEach((key, value) -> {
            switch (key) {
                case "username":
                    user.setUsername((String) value);
                    break;
                case "email":
                    user.setEmail((String) value);
                    break;
                case "phoneNumber":
                    user.setPhoneNumber((String) value);
                    break;
                case "county":
                    user.setCounty((String) value);
                    break;
                case "city":
                    user.setCity((String) value);
                    break;
                case "shelterType":
                    user.setShelterType((String) value);
                    break;
                case "biography":
                    user.setBiography((String) value);
                    break;
                case "fullAddress":
                    user.setFullAddress((String) value);
                    break;
                case "zipCode":
                    user.setZipCode((String) value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown field: " + key);
            }
        });

        return userRepository.save(user);
    }


    @Override
    public void uploadProfilePicture(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            user.setProfilePicture(file.getBytes());
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    @Override
    public byte[] getProfilePicture(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getProfilePicture();
    }

    @Override
    public void deleteProfilePicture(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setProfilePicture(null);
        userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Verificare dacă parola curentă este corectă
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        // Verificare dacă noua parolă este identică cu cea anterioară
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }

        // Verificare cerințe parolă
        if (newPassword.length() < 10 || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*[!@#?].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password does not meet the required criteria");
        }

        // Salvarea parolei criptate
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public User getShelterById(Long shelterId) {
        User shelter = userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        if (!shelter.getRole().equals(Role.SHELTER)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a shelter");
        }

        return shelter;
    }

    public List<UserDTO> getAllShelters() {
        List<User> shelters = userRepository.findByRole(Role.SHELTER);
        return shelters.stream().map(UserDTO::new).toList();
    }




}
