package com.example.payments.auth;

public record LoginResponse(String token, String tokenType, String email, String role) {}
