package com.rescueme.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors().configurationSource(corsConfigurationSource()).and()
                .authorizeHttpRequests(auth -> auth
                        // Rute publice
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/v1/auth/register/**").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/auth/refresh-token").permitAll()
                        .requestMatchers("/api/v1/auth/check-email").permitAll()
                        .requestMatchers("/api/v1/auth/check-username").permitAll()
                        .requestMatchers("/api/v1/auth/request-reset").permitAll()
                        .requestMatchers("/api/v1/auth/reset-password").permitAll()
                        .requestMatchers("/api/v1/auth/change-password").permitAll()

                        // Permite GET pe /api/v1/events
                        .requestMatchers(HttpMethod.GET, "/api/v1/events").permitAll()
                        .requestMatchers("/api/v1/events/**").authenticated()

                        // Permite GET pe /pets/available
                        .requestMatchers(HttpMethod.GET, "/pets/available").permitAll()
                        .requestMatchers("/pets/**").authenticated()

                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/pet-photos/**").authenticated()
                        .requestMatchers("/api/test-notifications/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/v1/messages/**").authenticated()
                        .requestMatchers("/api/v1/donations/**").authenticated()
                        .requestMatchers("/api/v1/attachments/**").authenticated()
                        .requestMatchers("/api/v1/shelters/**").authenticated()
                        .requestMatchers("/api/v1/favorites/**").authenticated()
                        .requestMatchers("/api/v1/adoptions/**").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/uploads/**");
    }
}
