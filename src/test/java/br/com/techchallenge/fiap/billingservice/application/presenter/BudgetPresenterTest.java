package br.com.techchallenge.fiap.billingservice.application.presenter;

import br.com.techchallenge.fiap.billingservice.application.dto.BudgetDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BudgetPresenter - Unit Tests")
class BudgetPresenterTest {

    @Test
    @DisplayName("Should convert Budget to BudgetDto")
    void shouldConvertBudgetToDto() {
        // Arrange
        Budget budget = createBudget();

        // Act
        BudgetDto dto = BudgetPresenter.toDto(budget);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.budgetId()).isEqualTo(budget.budgetId());
        assertThat(dto.serviceOrderId()).isEqualTo(budget.serviceOrderId());
        assertThat(dto.totalAmount()).isEqualByComparingTo(budget.totalAmount().value());
        assertThat(dto.status()).isEqualTo(budget.status().name());
    }

    @Test
    @DisplayName("Should return null when budget is null")
    void shouldReturnNullWhenBudgetIsNull() {
        // Act
        BudgetDto dto = BudgetPresenter.toDto(null);

        // Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("Should convert list of Budgets to list of DTOs")
    void shouldConvertListOfBudgetsToListOfDtos() {
        // Arrange
        List<Budget> budgets = Arrays.asList(
            createBudget("BUDGET-001"),
            createBudget("BUDGET-002")
        );

        // Act
        List<BudgetDto> dtos = BudgetPresenter.toDtoList(budgets);

        // Assert
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).budgetId()).isEqualTo("BUDGET-001");
        assertThat(dtos.get(1).budgetId()).isEqualTo("BUDGET-002");
    }

    @Test
    @DisplayName("Should return empty list when budget list is null")
    void shouldReturnEmptyListWhenBudgetListIsNull() {
        // Act
        List<BudgetDto> dtos = BudgetPresenter.toDtoList(null);

        // Assert
        assertThat(dtos).isEmpty();
    }

    private Budget createBudget() {
        return createBudget("BUDGET-001");
    }

    private Budget createBudget(String budgetId) {
        return Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of())
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
