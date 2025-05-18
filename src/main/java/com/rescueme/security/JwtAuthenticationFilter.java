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

        // extrage token din cookie sau header

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        // Caz special pentru token-ul admin
        if ("admin-token".equals(jwt)) {
            // Găsește utilizatorul admin din baza de date
            User adminUser = userRepository.findByEmail("rescueme.care@gmail.com")
                    .orElse(null);

            if (adminUser != null && adminUser.getRole() == Role.ADMIN) {
                UserPrincipal userPrincipal = new UserPrincipal(adminUser);
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ADMIN")
                );

                var authToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
            return;
        }

        // verifica tokenul & autentifica userul
        if (jwt != null) {
            try {
                String username = jwtUtil.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var user = userRepository.findByEmail(username).orElse(null);
                    if (user != null && jwtUtil.validateToken(jwt, user)) {
                        UserPrincipal userPrincipal = new UserPrincipal(user);

                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority(user.getRole().toString())
                        );

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
                    }
                }
            } catch (ExpiredJwtException e) {
                System.out.println("JWT expired: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error processing JWT: " + e.getMessage());
            }
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