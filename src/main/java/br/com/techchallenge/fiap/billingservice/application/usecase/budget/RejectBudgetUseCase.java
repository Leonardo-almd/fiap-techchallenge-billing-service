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
 * Use case for rejecting a budget.
 */
@RequiredArgsConstructor
public class RejectBudgetUseCase {

    private final BudgetGateway budgetGateway;
    private Consumer<Budget> onRejectionCallback;

    /**
     * Execute the use case to reject a budget.
     * 
     * @param budgetId Budget ID to reject
     * @return Rejected budget
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

        // Update status to REJECTED
        Budget rejectedBudget = budget.withStatusUpdated(
            BudgetStatus.rejected(),
            LocalDateTime.now()
        );

        // Save
        Budget savedBudget = budgetGateway.update(rejectedBudget);

        // Trigger callback (for event publishing)
        if (onRejectionCallback != null) {
            onRejectionCallback.accept(savedBudget);
        }

        return savedBudget;
    }

    /**
     * Set callback to be called after rejection (used for event publishing).
     */
    public void setOnRejectionCallback(Consumer<Budget> callback) {
        this.onRejectionCallback = callback;
    }
}
