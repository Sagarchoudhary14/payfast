package com.example.payments.payment;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    @Test
    void returnsExistingTransactionForRepeatedIdempotencyKey() {
        PaymentRepository repository = mock(PaymentRepository.class);
        FraudCheckService fraud = mock(FraudCheckService.class);
        PaymentTransaction existing = new PaymentTransaction("customer@example.com", "merchant-1",
            new BigDecimal("25.00"), "INR", "test", PaymentStatus.AUTHORIZED, null, "same-key");
        when(repository.findByUserEmailAndIdempotencyKey("customer@example.com", "same-key"))
            .thenReturn(Optional.of(existing));

        PaymentService service = new PaymentService(repository, fraud);
        PaymentResponse response = service.create("customer@example.com", "same-key",
            new CreatePaymentRequest("merchant-1", new BigDecimal("25.00"), "INR", "test"));

        assertEquals(PaymentStatus.AUTHORIZED, response.status());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(fraud);
    }

    @Test
    void requiresIdempotencyKey() {
        PaymentService service = new PaymentService(mock(PaymentRepository.class), mock(FraudCheckService.class));
        assertThrows(IllegalArgumentException.class, () -> service.create("customer@example.com", " ",
            new CreatePaymentRequest("merchant-1", new BigDecimal("25.00"), "INR", "test")));
    }
}
