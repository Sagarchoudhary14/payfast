package com.example.payments.payment;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final FraudCheckService fraudChecks;

    public PaymentService(PaymentRepository payments, FraudCheckService fraudChecks) {
        this.payments = payments;
        this.fraudChecks = fraudChecks;
    }

    @Transactional
    public PaymentResponse create(String userEmail, String idempotencyKey, CreatePaymentRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException("A valid Idempotency-Key header (1-100 characters) is required");
        }

        var existing = payments.findByUserEmailAndIdempotencyKey(userEmail, idempotencyKey);
        if (existing.isPresent()) return PaymentResponse.from(existing.get());

        String reason = fraudChecks.declineReason(userEmail, request.recipient().trim(), request.amount());
        PaymentStatus status = reason == null ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
        var transaction = new PaymentTransaction(userEmail, request.recipient().trim(), request.amount(),
            request.currency(), request.description(), status, reason, idempotencyKey);

        try {
            return PaymentResponse.from(payments.saveAndFlush(transaction));
        } catch (DataIntegrityViolationException race) {
            // The unique database constraint protects against duplicate keys in concurrent requests.
            // A production implementation should retry/read the winner in a new transaction.
            return payments.findByUserEmailAndIdempotencyKey(userEmail, idempotencyKey)
                .map(PaymentResponse::from)
                .orElseThrow(() -> race);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> list(String userEmail, boolean isAdmin) {
        var result = isAdmin ? payments.findAllByOrderByCreatedAtDesc()
                             : payments.findByUserEmailOrderByCreatedAtDesc(userEmail);
        return result.stream().map(PaymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Long id, String userEmail, boolean isAdmin) {
        var payment = payments.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
        if (!isAdmin && !payment.getUserEmail().equals(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access this transaction");
        }
        return PaymentResponse.from(payment);
    }
}
