package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.Builder;

/**
 * Budget aggregate root entity.
 * Represents a budget for a service order with business rules and validations.
 */
public record Budget(
    String budgetId,
    String serviceOrderId,
    String customerId,
    String vehicleId,
    List<BudgetItem> items,
    Price totalAmount,
    BudgetStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime approvedAt,
    LocalDateTime rejectedAt
) {

    public Budget {
        if (Objects.isNull(serviceOrderId) || serviceOrderId.isBlank()) {
            throw new InvalidDataException("Budget serviceOrderId must not be null or blank");
        }
        if (Objects.isNull(customerId) || customerId.isBlank()) {
            throw new InvalidDataException("Budget customerId must not be null or blank");
        }
        if (Objects.isNull(vehicleId) || vehicleId.isBlank()) {
            throw new InvalidDataException("Budget vehicleId must not be null or blank");
        }
        if (Objects.isNull(items) || items.isEmpty()) {
            throw new InvalidDataException("Budget must have at least one item");
        }
        if (Objects.isNull(totalAmount)) {
            throw new InvalidDataException("Budget totalAmount must not be null");
        }
        if (Objects.isNull(status)) {
            throw new InvalidDataException("Budget status must not be null");
        }
    }

    @Builder(builderMethodName = "builder")
    public static Budget buildBudget(
            String budgetId,
            String serviceOrderId,
            String customerId,
            String vehicleId,
            List<BudgetItem> items,
            BigDecimal totalAmount,
            BudgetStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime approvedAt,
            LocalDateTime rejectedAt) {
        Price price = totalAmount == null ? null : new Price(totalAmount);
        return new Budget(
            budgetId,
            serviceOrderId,
            customerId,
            vehicleId,
            items,
            price,
            status,
            createdAt,
            updatedAt,
            approvedAt,
            rejectedAt
        );
    }

    public Budget withId(String budgetId) {
        return new Budget(
            budgetId,
            this.serviceOrderId,
            this.customerId,
            this.vehicleId,
            this.items,
            this.totalAmount,
            this.status,
            this.createdAt,
            this.updatedAt,
            this.approvedAt,
            this.rejectedAt
        );
    }

    /**
     * Return a new Budget with the status changed to newStatus and timestamps
     * adjusted according to business rules:
     * - updatedAt is set to now
     * - when transitioning to APPROVED, approvedAt is set to now if not already set
     * - when transitioning to REJECTED, rejectedAt is set to now if not already set
     */
    public Budget withStatusUpdated(BudgetStatus newStatus, LocalDateTime now) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }

        LocalDateTime approved = this.approvedAt;
        LocalDateTime rejected = this.rejectedAt;

        if (newStatus.isApproved() && approved == null) {
            approved = now;
        }

        if (newStatus.isRejected() && rejected == null) {
            rejected = now;
        }

        return new Budget(
            this.budgetId,
            this.serviceOrderId,
            this.customerId,
            this.vehicleId,
            this.items,
            this.totalAmount,
            newStatus,
            this.createdAt,
            now,  // updatedAt
            approved,
            rejected
        );
    }

    /**
     * Calculate total amount from all items.
     */
    public static Price calculateTotalAmount(List<BudgetItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidDataException("Cannot calculate total from empty items list");
        }
        
        return items.stream()
            .map(BudgetItem::totalPrice)
            .reduce(Price.zero(), Price::add);
    }
}
