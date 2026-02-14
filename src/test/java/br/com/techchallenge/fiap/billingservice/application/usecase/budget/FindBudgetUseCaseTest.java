package br.com.techchallenge.fiap.billingservice.application.usecase.budget;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetStatus;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindBudgetUseCase - Unit Tests")
class FindBudgetUseCaseTest {

    @Mock
    private BudgetGateway budgetGateway;

    private FindBudgetUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindBudgetUseCase(budgetGateway);
    }

    @Test
    @DisplayName("Should find budget by ID")
    void shouldFindBudgetById() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget budget = createBudget(budgetId);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(budget));

        // Act
        Budget result = useCase.findById(budgetId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo(budgetId);
        verify(budgetGateway).findById(budgetId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when budget not found")
    void shouldThrowNotFoundExceptionWhenBudgetNotFound() {
        // Arrange
        String budgetId = "NON-EXISTENT";
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.findById(budgetId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Budget not found");
        
        verify(budgetGateway).findById(budgetId);
    }

    @Test
    @DisplayName("Should find budgets by service order ID")
    void shouldFindBudgetsByServiceOrderId() {
        // Arrange
        String serviceOrderId = "ORDER-001";
        List<Budget> budgets = Arrays.asList(
            createBudget("BUDGET-001"),
            createBudget("BUDGET-002")
        );
        when(budgetGateway.findByServiceOrderId(serviceOrderId)).thenReturn(budgets);

        // Act
        List<Budget> result = useCase.findByServiceOrderId(serviceOrderId);

        // Assert
        assertThat(result).hasSize(2);
        verify(budgetGateway).findByServiceOrderId(serviceOrderId);
    }

    @Test
    @DisplayName("Should find all budgets with pagination")
    void shouldFindAllBudgetsWithPagination() {
        // Arrange
        List<Budget> budgets = Arrays.asList(createBudget("B1"), createBudget("B2"));
        when(budgetGateway.findAll(0, 15)).thenReturn(budgets);
        when(budgetGateway.count()).thenReturn(2L);

        // Act
        List<Budget> result = useCase.findAll(0, 15);
        long count = useCase.count();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should throw exception for invalid page number")
    void shouldThrowExceptionForInvalidPageNumber() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.findAll(-1, 15))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page number must not be negative");
    }

    @Test
    @DisplayName("Should throw exception for invalid page size")
    void shouldThrowExceptionForInvalidPageSize() {
        // Act & Assert
        assertThatThrownBy(() -> useCase.findAll(0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page size must be positive");
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
