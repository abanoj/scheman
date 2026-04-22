package com.abanoj.scheman.auth.service;

import com.abanoj.scheman.config.AdminProperties;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AdminInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Bean
    public CommandLineRunner createAdminUser(){
        return args -> {
            if (userRepository.existsByEmail(adminProperties.getEmail())){
                log.info("User email already exists, skipping creation");
                return;
            }
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Admin")
                    .email(adminProperties.getEmail())
                    .password(passwordEncoder.encode(adminProperties.getPassword()))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Created user admin with email: {}", admin.getEmail());
        };
    }

}
