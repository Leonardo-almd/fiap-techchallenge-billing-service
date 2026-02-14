package br.com.techchallenge.fiap.billingservice.application.usecase.budget;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItem;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetStatus;
import br.com.techchallenge.fiap.billingservice.application.entity.Price;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Use case for creating a new budget.
 */
@RequiredArgsConstructor
public class CreateBudgetUseCase {

    private final BudgetGateway budgetGateway;

    /**
     * Execute the use case to create a budget.
     * 
     * @param serviceOrderId Service order ID
     * @param customerId Customer ID
     * @param vehicleId Vehicle ID
     * @param items List of budget items
     * @return Created budget
     */
    public Budget execute(String serviceOrderId, String customerId, String vehicleId, 
                         List<BudgetItemRequest> items) {
        
        if (items == null || items.isEmpty()) {
            throw new InvalidDataException("Budget must have at least one item");
        }

        // Create budget items
        List<BudgetItem> budgetItems = new ArrayList<>();
        
        for (BudgetItemRequest itemRequest : items) {
            Price unitPrice = new Price(itemRequest.unitPrice());
            Price totalPrice = unitPrice.multiply(itemRequest.quantity());
            
            BudgetItem item = new BudgetItem(
                UUID.randomUUID().toString(),
                itemRequest.type(),
                itemRequest.itemCode(),
                itemRequest.description(),
                itemRequest.quantity(),
                unitPrice,
                totalPrice
            );
            budgetItems.add(item);
        }

        // Calculate total amount
        Price totalAmount = Budget.calculateTotalAmount(budgetItems);

        // Create budget
        LocalDateTime now = LocalDateTime.now();
        Budget budget = Budget.builder()
            .budgetId(UUID.randomUUID().toString())
            .serviceOrderId(serviceOrderId)
            .customerId(customerId)
            .vehicleId(vehicleId)
            .items(budgetItems)
            .totalAmount(totalAmount.value())
            .status(BudgetStatus.pendingApproval())
            .createdAt(now)
            .updatedAt(now)
            .build();

        // Save budget
        return budgetGateway.save(budget);
    }

    /**
     * Request data for creating a budget item.
     */
    public record BudgetItemRequest(
        BudgetItemType type,
        String itemCode,
        String description,
        Integer quantity,
        java.math.BigDecimal unitPrice
    ) {}
}
