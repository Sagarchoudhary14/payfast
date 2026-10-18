package com.example.payments.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/health")
    public Map<String, String> adminHealth() {
        return Map.of("status", "UP", "scope", "ADMIN");
    }
}
