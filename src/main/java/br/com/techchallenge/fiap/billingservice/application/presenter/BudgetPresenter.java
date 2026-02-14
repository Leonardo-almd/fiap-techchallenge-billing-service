package br.com.techchallenge.fiap.billingservice.application.presenter;

import br.com.techchallenge.fiap.billingservice.application.dto.BudgetDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetItemDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItem;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter for converting Budget entities to DTOs.
 */
public class BudgetPresenter {

    public static BudgetDto toDto(Budget budget) {
        if (budget == null) {
            return null;
        }

        List<BudgetItemDto> itemDtos = budget.items().stream()
            .map(BudgetPresenter::toItemDto)
            .collect(Collectors.toList());

        return new BudgetDto(
            budget.budgetId(),
            budget.serviceOrderId(),
            budget.customerId(),
            budget.vehicleId(),
            itemDtos,
            budget.totalAmount().value(),
            budget.status().name(),
            budget.createdAt(),
            budget.updatedAt(),
            budget.approvedAt(),
            budget.rejectedAt()
        );
    }

    public static BudgetItemDto toItemDto(BudgetItem item) {
        if (item == null) {
            return null;
        }

        return new BudgetItemDto(
            item.itemId(),
            item.type(),
            item.itemCode(),
            item.description(),
            item.quantity(),
            item.unitPrice().value(),
            item.totalPrice().value()
        );
    }

    public static List<BudgetDto> toDtoList(List<Budget> budgets) {
        if (budgets == null) {
            return List.of();
        }

        return budgets.stream()
            .map(BudgetPresenter::toDto)
            .collect(Collectors.toList());
    }
}
