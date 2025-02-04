package com.rescueme.security;

import com.rescueme.exception.IncorrectCredentialsException;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.User;
import com.rescueme.security.request.LoginRequest;
import com.rescueme.security.response.LoginResponse;
import com.rescueme.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IncorrectCredentialsException("Incorrect credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IncorrectCredentialsException("Incorrect credentials");
        }

        String token = jwtUtil.generateToken(user);

        return new LoginResponse(token, user.getRole().toString(), user.getUsername(), user.getId());
    }

}
