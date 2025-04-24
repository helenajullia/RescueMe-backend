package com.rescueme.security;

import com.rescueme.exception.IncorrectCredentialsException;
import com.rescueme.repository.RefreshTokenRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.RefreshToken;
import com.rescueme.repository.entity.User;
import com.rescueme.security.request.LoginRequest;
import com.rescueme.security.response.LoginResponse;
import com.rescueme.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IncorrectCredentialsException("Incorrect credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IncorrectCredentialsException("Incorrect credentials");
        }

        String accessToken = jwtUtil.generateToken(user); // 15 min
        String refreshToken = jwtUtil.generateRefreshToken(user); // 7 zile

        return new LoginResponse(accessToken, refreshToken, user.getRole().toString(), user.getUsername(), user.getId());
    }

    public RefreshToken saveRefreshToken(String token, User user, long expirationMillis) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(expirationMillis))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    public boolean isRefreshTokenValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked() && rt.getExpiryDate().isAfter(Instant.now()))
                .isPresent();
    }

    public boolean validateRefreshToken(String token) {
        return isRefreshTokenValid(token);
    }

    public String extractUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    public String generateAccessToken(User user) {
        return jwtUtil.generateToken(user);
    }
}
