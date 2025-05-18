package com.rescueme.security;

import com.rescueme.exception.IncorrectCredentialsException;
import com.rescueme.repository.RefreshTokenRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.RefreshToken;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.User;
import com.rescueme.security.request.LoginRequest;
import com.rescueme.security.response.LoginResponse;
import com.rescueme.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

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

        // Verificare specială pentru admin - adaugă un ID special pentru frontend
        String idToSend = user.getRole() == Role.ADMIN ? "admin-id" : user.getId().toString();

        String accessToken = jwtUtil.generateToken(user); // 1 zi
        String refreshToken = jwtUtil.generateRefreshToken(user); // 7 zile

        // Salvăm token-ul de refresh
        saveRefreshToken(refreshToken, user, 1000L * 60 * 60 * 24 * 7);

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getRole().toString(),
                user.getUsername(),
                user.getRole() == Role.ADMIN ? -1L : user.getId() // ID special pentru admin
        );
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
        try {
            Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
            if (refreshTokenOpt.isEmpty()) {
                System.out.println("Refresh token not found in database: " + token);
                return false;
            }

            RefreshToken refreshToken = refreshTokenOpt.get();
            boolean isNotRevoked = !refreshToken.isRevoked();
            boolean isNotExpired = refreshToken.getExpiryDate().isAfter(Instant.now());

            System.out.println("Refresh token found. Revoked: " + refreshToken.isRevoked()
                    + ", Expired: " + !isNotExpired);

            return isNotRevoked && isNotExpired;
        } catch (Exception e) {
            System.out.println("Error validating refresh token: " + e.getMessage());
            return false;
        }
    }

    public String extractUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    public String generateAccessToken(User user) {
        return jwtUtil.generateToken(user);
    }
}
