package br.com.techchallenge.fiap.billingservice.application.usecase.budget;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetStatus;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Use case for approving a budget.
 * Extracted from the monolith's ProcessApprovalUseCase.
 */
@RequiredArgsConstructor
public class ApproveBudgetUseCase {

    private final BudgetGateway budgetGateway;
    private Consumer<Budget> onApprovalCallback;

    /**
     * Execute the use case to approve a budget.
     * 
     * @param budgetId Budget ID to approve
     * @return Approved budget
     * @throws NotFoundException if budget not found
     * @throws InvalidDataException if budget is not pending approval
     */
    public Budget execute(String budgetId) {
        // Find budget
        Budget budget = budgetGateway.findById(budgetId)
            .orElseThrow(() -> new NotFoundException("Budget not found with id: " + budgetId));

        // Validate status
        if (!budget.status().isPendingApproval()) {
            throw new InvalidDataException(
                "Budget is not pending approval. Current status: " + budget.status().name()
            );
        }

        // Update status to APPROVED
        Budget approvedBudget = budget.withStatusUpdated(
            BudgetStatus.approved(),
            LocalDateTime.now()
        );

        // Save
        Budget savedBudget = budgetGateway.update(approvedBudget);

        // Trigger callback (for event publishing)
        if (onApprovalCallback != null) {
            onApprovalCallback.accept(savedBudget);
        }

        return savedBudget;
    }

    /**
     * Set callback to be called after approval (used for event publishing).
     */
    public void setOnApprovalCallback(Consumer<Budget> callback) {
        this.onApprovalCallback = callback;
    }
}
