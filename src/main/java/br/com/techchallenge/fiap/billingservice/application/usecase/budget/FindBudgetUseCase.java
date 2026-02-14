package br.com.techchallenge.fiap.billingservice.application.usecase.budget;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Use case for finding budgets.
 */
@RequiredArgsConstructor
public class FindBudgetUseCase {

    private final BudgetGateway budgetGateway;

    /**
     * Find a budget by ID.
     */
    public Budget findById(String budgetId) {
        return budgetGateway.findById(budgetId)
            .orElseThrow(() -> new NotFoundException("Budget not found with id: " + budgetId));
    }

    /**
     * Find all budgets for a service order.
     */
    public List<Budget> findByServiceOrderId(String serviceOrderId) {
        return budgetGateway.findByServiceOrderId(serviceOrderId);
    }

    /**
     * Find all budgets (paginated).
     */
    public List<Budget> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        
        return budgetGateway.findAll(page, size);
    }

    /**
     * Count total budgets.
     */
    public long count() {
        return budgetGateway.count();
    }
}
