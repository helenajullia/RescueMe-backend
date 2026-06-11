package com.rescueme.security;

import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.User;
import com.rescueme.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = null;

        try {
            // Extract token from cookie or header
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("accessToken".equals(cookie.getName())) {
                        jwt = cookie.getValue();
//                        System.out.println("Found JWT in cookie: " + jwt.substring(0, Math.min(10, jwt.length())) + "...");
                        break;
                    }
                }
            }

            // Verifică dacă tokenul este furnizat ca parametru în URL (pentru descărcări imagini)
            if (jwt == null && request.getParameter("token") != null) {
                jwt = request.getParameter("token");
            }

            if (jwt == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwt = authHeader.substring(7);
//                    System.out.println("Found JWT in Authorization header: " + jwt.substring(0, Math.min(10, jwt.length())) + "...");
                }
            }

            // Validate token and authenticate user
            if (jwt != null) {
                try {
                    String username = jwtUtil.extractUsername(jwt);
//                    System.out.println("Extracted username from JWT: " + username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        var user = userRepository.findByEmail(username).orElse(null);

                        if (user == null) {
//                            System.out.println("User not found for email: " + username);
                        } else {
                            boolean isTokenValid = jwtUtil.validateToken(jwt, user);
//                            System.out.println("JWT validation result for user " + user.getUsername() + ": " + isTokenValid);

                            if (isTokenValid) {
                                UserPrincipal userPrincipal = new UserPrincipal(user);

                                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                                authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));

                                // role-specific authorities
                                if (user.getRole() == Role.ADMIN) {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                                } else if (user.getRole() == Role.SHELTER) {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_SHELTER"));
                                } else {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_ADOPTER"));
                                }

                                var authToken = new UsernamePasswordAuthenticationToken(
                                        userPrincipal, null, authorities
                                );
                                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authToken);
//                                System.out.println("Successfully authenticated user: " + user.getUsername());
                            }
                        }
                    }
                } catch (ExpiredJwtException e) {
//                    System.out.println("JWT expired: " + e.getMessage());
                } catch (Exception e) {
//                    System.out.println("Error processing JWT: " + e.getMessage());
//                    e.printStackTrace(); // Print stack trace for more detailed error info
                }
            } else {
//                System.out.println("No JWT token found for request: " + request.getRequestURI());
            }
        } catch (Exception e) {
//            System.out.println("General exception in JwtAuthenticationFilter: " + e.getMessage());
//            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> publicPaths = Arrays.asList(
                "/api/v1/auth/login",
                "/api/v1/auth/register/adopter",
                "/api/v1/auth/register/shelter",
                "/api/v1/auth/refresh-token",
                "/api/v1/auth/check-email",
                "/api/v1/auth/check-username",
                "/api/v1/auth/request-reset",
                "/api/v1/auth/reset-password"
        );

        return publicPaths.contains(path);
    }
}