package com.example.payments.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(Long id, String recipient, BigDecimal amount, String currency,
                               String description, PaymentStatus status, String fraudReason,
                               Instant createdAt) {
    public static PaymentResponse from(PaymentTransaction t) {
        return new PaymentResponse(t.getId(), t.getRecipient(), t.getAmount(), t.getCurrency(),
            t.getDescription(), t.getStatus(), t.getFraudReason(), t.getCreatedAt());
    }
}
