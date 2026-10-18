package com.example.payments.payment;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping
    @Operation(summary = "Authorize a payment; idempotent for a user and Idempotency-Key")
    public PaymentResponse create(@RequestHeader(value = "Idempotency-Key", required = false) String key,
                                  @Valid @RequestBody CreatePaymentRequest request,
                                  Authentication authentication) {
        return paymentService.create(authentication.getName(), key, request);
    }

    @GetMapping
    @Operation(summary = "List transactions belonging to the current user; admins see all")
    public List<PaymentResponse> list(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return paymentService.list(authentication.getName(), admin);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return paymentService.get(id, authentication.getName(), admin);
    }
}
