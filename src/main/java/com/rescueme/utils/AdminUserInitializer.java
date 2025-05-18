package com.rescueme.utils;

import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {

    @Bean
    public CommandLineRunner initAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.findByEmail("rescueme.care@gmail.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setEmail("rescueme.care@gmail.com");
                adminUser.setUsername("Admin");
                adminUser.setPassword(passwordEncoder.encode("ParolaAdmin!"));
                adminUser.setRole(Role.ADMIN);

                userRepository.save(adminUser);
                System.out.println("Admin user created successfully");
            } else {
                System.out.println("Admin user already exists");
            }
        };
    }
}