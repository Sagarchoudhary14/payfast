package com.example.payments.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByUserEmailAndIdempotencyKey(String userEmail, String idempotencyKey);
    List<PaymentTransaction> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();
    long countByUserEmailAndRecipientAndCreatedAtAfter(String userEmail, String recipient, Instant after);
}
