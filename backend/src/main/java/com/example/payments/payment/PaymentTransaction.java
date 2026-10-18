package com.example.payments.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions",
       indexes = {@Index(name = "idx_payment_user_created", columnList = "userEmail, createdAt"),
                  @Index(name = "idx_payment_recipient_created", columnList = "recipient, createdAt")},
       uniqueConstraints = @UniqueConstraint(name = "uk_user_idempotency", columnNames = {"userEmail", "idempotencyKey"}))
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 190)
    private String userEmail;

    @Column(nullable = false, length = 120)
    private String recipient;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 250)
    private String fraudReason;

    @Column(nullable = false, length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected PaymentTransaction() {}

    public PaymentTransaction(String userEmail, String recipient, BigDecimal amount, String currency,
                              String description, PaymentStatus status, String fraudReason, String idempotencyKey) {
        this.userEmail = userEmail;
        this.recipient = recipient;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.status = status;
        this.fraudReason = fraudReason;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getRecipient() { return recipient; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public PaymentStatus getStatus() { return status; }
    public String getFraudReason() { return fraudReason; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
}
