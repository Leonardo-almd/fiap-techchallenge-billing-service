package br.com.techchallenge.fiap.billingservice.application.gateway;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import java.util.List;
import java.util.Optional;

/**
 * Gateway (port) for Budget persistence operations.
 * This is an interface in the application layer that will be implemented
 * by the infrastructure layer (e.g., DynamoDB repository).
 */
public interface BudgetGateway {

    /**
     * Save a new budget or update an existing one.
     */
    Budget save(Budget budget);

    /**
     * Update an existing budget.
     */
    Budget update(Budget budget);

    /**
     * Find a budget by ID.
     */
    Optional<Budget> findById(String budgetId);

    /**
     * Find all budgets for a service order.
     */
    List<Budget> findByServiceOrderId(String serviceOrderId);

    /**
     * Find all budgets (paginated).
     */
    List<Budget> findAll(int page, int size);

    /**
     * Count total budgets.
     */
    long count();

    /**
     * Delete a budget by ID.
     */
    void deleteById(String budgetId);
}
