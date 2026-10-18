package com.example.payments.payment;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) { super("Payment transaction " + id + " was not found"); }
}
