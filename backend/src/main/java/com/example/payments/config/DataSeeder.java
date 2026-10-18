package com.example.payments.config;

import com.example.payments.auth.AppUser;
import com.example.payments.auth.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedUsers(AppUserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmail("customer@example.com")) {
                users.save(new AppUser("customer@example.com", encoder.encode("Customer123!"), "CUSTOMER"));
            }
            if (!users.existsByEmail("admin@example.com")) {
                users.save(new AppUser("admin@example.com", encoder.encode("Admin123!"), "ADMIN"));
            }
        };
    }
}
