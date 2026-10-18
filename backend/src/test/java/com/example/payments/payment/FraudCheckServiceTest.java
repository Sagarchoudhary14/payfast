package com.example.payments.payment;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudCheckServiceTest {
    @Test
    void declinesAmountAboveConfiguredLimit() {
        PaymentRepository repository = mock(PaymentRepository.class);
        FraudCheckService service = new FraudCheckService(repository);

        String reason = service.declineReason("customer@example.com", "merchant-1", new BigDecimal("100000.01"));

        assertNotNull(reason);
        assertTrue(reason.contains("limit"));
        verifyNoInteractions(repository);
    }

    @Test
    void authorizesAmountWhenNoFraudRuleMatches() {
        PaymentRepository repository = mock(PaymentRepository.class);
        when(repository.countByUserEmailAndRecipientAndCreatedAtAfter(anyString(), anyString(), any()))
            .thenReturn(0L);
        FraudCheckService service = new FraudCheckService(repository);

        assertNull(service.declineReason("customer@example.com", "merchant-1", new BigDecimal("250.00")));
    }

    @Test
    void declinesRepeatedRecipientPayments() {
        PaymentRepository repository = mock(PaymentRepository.class);
        when(repository.countByUserEmailAndRecipientAndCreatedAtAfter(anyString(), anyString(), any()))
            .thenReturn(3L);
        FraudCheckService service = new FraudCheckService(repository);

        String reason = service.declineReason("customer@example.com", "merchant-1", new BigDecimal("250.00"));

        assertNotNull(reason);
        assertTrue(reason.contains("recent payments"));
    }
}
