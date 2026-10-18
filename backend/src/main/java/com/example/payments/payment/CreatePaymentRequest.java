package com.example.payments.payment;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePaymentRequest(
    @NotBlank @Size(max = 120) String recipient,
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
    @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter uppercase code") String currency,
    @Size(max = 500) String description
) {}
