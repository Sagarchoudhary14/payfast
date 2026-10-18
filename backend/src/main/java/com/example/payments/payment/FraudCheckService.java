package com.example.payments.payment;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;

@Service
public class FraudCheckService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000.00");

    private final PaymentRepository payments;

    public FraudCheckService(PaymentRepository payments) {
        this.payments = payments;
    }

    public String declineReason(String userEmail, String recipient, BigDecimal amount) {
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            return "Amount exceeds the configured transaction limit";
        }
        long recentPayments = payments.countByUserEmailAndRecipientAndCreatedAtAfter(
            userEmail, recipient, Instant.now().minusSeconds(5 * 60));
        if (recentPayments >= 3) {
            return "Too many recent payments to this recipient";
        }
        return null;
    }
}
