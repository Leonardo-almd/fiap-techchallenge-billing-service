package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;

/**
 * Payment aggregate root entity.
 * Represents a payment transaction with business rules and validations.
 */
public record Payment(
    String paymentId,
    String budgetId,
    String serviceOrderId,
    Price amount,
    PaymentMethod method,
    PaymentStatus status,
    String externalId,
    String authorizationCode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime processedAt,
    LocalDateTime refundedAt,
    String failureReason
) {

    public Payment {
        if (Objects.isNull(budgetId) || budgetId.isBlank()) {
            throw new InvalidDataException("Payment budgetId must not be null or blank");
        }
        if (Objects.isNull(serviceOrderId) || serviceOrderId.isBlank()) {
            throw new InvalidDataException("Payment serviceOrderId must not be null or blank");
        }
        if (Objects.isNull(amount)) {
            throw new InvalidDataException("Payment amount must not be null");
        }
        if (amount.isZero()) {
            throw new InvalidDataException("Payment amount must be greater than zero");
        }
        if (Objects.isNull(method)) {
            throw new InvalidDataException("Payment method must not be null");
        }
        if (Objects.isNull(status)) {
            throw new InvalidDataException("Payment status must not be null");
        }
    }

    @Builder(builderMethodName = "builder")
    public static Payment buildPayment(
            String paymentId,
            String budgetId,
            String serviceOrderId,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            String externalId,
            String authorizationCode,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime processedAt,
            LocalDateTime refundedAt,
            String failureReason) {
        Price price = amount == null ? null : new Price(amount);
        return new Payment(
            paymentId,
            budgetId,
            serviceOrderId,
            price,
            method,
            status,
            externalId,
            authorizationCode,
            createdAt,
            updatedAt,
            processedAt,
            refundedAt,
            failureReason
        );
    }

    public Payment withId(String paymentId) {
        return new Payment(
            paymentId,
            this.budgetId,
            this.serviceOrderId,
            this.amount,
            this.method,
            this.status,
            this.externalId,
            this.authorizationCode,
            this.createdAt,
            this.updatedAt,
            this.processedAt,
            this.refundedAt,
            this.failureReason
        );
    }

    public Payment withExternalId(String externalId) {
        return new Payment(
            this.paymentId,
            this.budgetId,
            this.serviceOrderId,
            this.amount,
            this.method,
            this.status,
            externalId,
            this.authorizationCode,
            this.createdAt,
            this.updatedAt,
            this.processedAt,
            this.refundedAt,
            this.failureReason
        );
    }

    public Payment withAuthorizationCode(String authorizationCode) {
        return new Payment(
            this.paymentId,
            this.budgetId,
            this.serviceOrderId,
            this.amount,
            this.method,
            this.status,
            this.externalId,
            authorizationCode,
            this.createdAt,
            this.updatedAt,
            this.processedAt,
            this.refundedAt,
            this.failureReason
        );
    }

    public Payment withFailureReason(String failureReason) {
        return new Payment(
            this.paymentId,
            this.budgetId,
            this.serviceOrderId,
            this.amount,
            this.method,
            this.status,
            this.externalId,
            this.authorizationCode,
            this.createdAt,
            this.updatedAt,
            this.processedAt,
            this.refundedAt,
            failureReason
        );
    }

    /**
     * Return a new Payment with the status changed to newStatus and timestamps
     * adjusted according to business rules:
     * - when transitioning to PAID, processedAt is set to now if not already set
     * - when transitioning to REFUNDED, refundedAt is set to now if not already set
     */
    public Payment withStatusUpdated(PaymentStatus newStatus, LocalDateTime now) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }

        LocalDateTime processed = this.processedAt;
        LocalDateTime refunded = this.refundedAt;

        if (newStatus.isPaid() && processed == null) {
            processed = now;
        }

        if (newStatus.isRefunded() && refunded == null) {
            refunded = now;
        }

        return new Payment(
            this.paymentId,
            this.budgetId,
            this.serviceOrderId,
            this.amount,
            this.method,
            newStatus,
            this.externalId,
            this.authorizationCode,
            this.createdAt,
            now, // updatedAt
            processed,
            refunded,
            this.failureReason
        );
    }
}
