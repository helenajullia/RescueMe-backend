package com.rescueme.utils;

import com.rescueme.repository.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("mySuperSecretKey1234567890123456789012345679")
    private String secretKey;

    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24; // 1 zi
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 zile

    public String generateToken(User user) {
        return createToken(new HashMap<>(), user.getEmail(), ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(User user) {
        return createToken(new HashMap<>(), user.getEmail(), REFRESH_TOKEN_EXPIRATION);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
        } catch (Exception e) {
            System.out.println("Error creating JWT token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Boolean validateToken(String token, User userDetails) {
        try {
            final String email = extractUsername(token);
            boolean valid = (email.equals(userDetails.getEmail()) && !isTokenExpired(token));
            return valid;
        } catch (Exception e) {
            System.out.println("Error validating token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.out.println("Error extracting username from token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            System.out.println("Error extracting claim from token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            System.out.println("Error parsing JWT claims: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            System.out.println("Error checking if token is expired: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}

