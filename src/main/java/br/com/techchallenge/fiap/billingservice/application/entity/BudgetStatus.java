package br.com.techchallenge.fiap.billingservice.application.entity;

/**
 * Budget status entity with business rules and transitions.
 */
public record BudgetStatus(BudgetStatusEnum status) {

    public static BudgetStatus pendingApproval() {
        return new BudgetStatus(BudgetStatusEnum.PENDING_APPROVAL);
    }

    public static BudgetStatus approved() {
        return new BudgetStatus(BudgetStatusEnum.APPROVED);
    }

    public static BudgetStatus rejected() {
        return new BudgetStatus(BudgetStatusEnum.REJECTED);
    }

    public boolean isPendingApproval() {
        return status == BudgetStatusEnum.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return status == BudgetStatusEnum.APPROVED;
    }

    public boolean isRejected() {
        return status == BudgetStatusEnum.REJECTED;
    }

    public String name() {
        return status.name();
    }
}
