package br.com.techchallenge.fiap.billingservice.application.usecase.budget;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetStatus;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveBudgetUseCase - Unit Tests")
class ApproveBudgetUseCaseTest {

    @Mock
    private BudgetGateway budgetGateway;

    private ApproveBudgetUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ApproveBudgetUseCase(budgetGateway);
    }

    @Test
    @DisplayName("Should approve budget when status is pending")
    void shouldApproveBudgetWhenStatusIsPending() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute(budgetId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.status().isApproved()).isTrue();
        assertThat(result.updatedAt()).isAfter(pendingBudget.updatedAt());

        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when budget does not exist")
    void shouldThrowNotFoundExceptionWhenBudgetDoesNotExist() {
        // Arrange
        String budgetId = "NON-EXISTENT";
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(budgetId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Budget not found");

        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway, never()).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when budget is already approved")
    void shouldThrowExceptionWhenBudgetIsAlreadyApproved() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(budgetId))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("not pending approval");

        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway, never()).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when budget is rejected")
    void shouldThrowExceptionWhenBudgetIsRejected() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget rejectedBudget = createRejectedBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(rejectedBudget));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(budgetId))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("not pending approval");

        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway, never()).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should call update gateway with approved budget")
    void shouldCallUpdateGatewayWithApprovedBudget() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);

        ArgumentCaptor<Budget> budgetCaptor = ArgumentCaptor.forClass(Budget.class);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(budgetId);

        // Assert
        verify(budgetGateway).update(budgetCaptor.capture());
        
        Budget updatedBudget = budgetCaptor.getValue();
        assertThat(updatedBudget.status().isApproved()).isTrue();
        assertThat(updatedBudget.budgetId()).isEqualTo(budgetId);
    }

    @Test
    @DisplayName("Should trigger callback after approval")
    void shouldTriggerCallbackAfterApproval() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock callback
        @SuppressWarnings("unchecked")
        Consumer<Budget> mockCallback = mock(Consumer.class);
        useCase.setOnApprovalCallback(mockCallback);

        // Act
        useCase.execute(budgetId);

        // Assert
        verify(mockCallback).accept(any(Budget.class));
    }

    @Test
    @DisplayName("Should not trigger callback if not set")
    void shouldNotTriggerCallbackIfNotSet() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - No exception should be thrown
        Budget result = useCase.execute(budgetId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.status().isApproved()).isTrue();
    }

    @Test
    @DisplayName("Should preserve all budget data during approval")
    void shouldPreserveAllBudgetDataDuringApproval() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute(budgetId);

        // Assert
        assertThat(result.budgetId()).isEqualTo(pendingBudget.budgetId());
        assertThat(result.serviceOrderId()).isEqualTo(pendingBudget.serviceOrderId());
        assertThat(result.customerId()).isEqualTo(pendingBudget.customerId());
        assertThat(result.vehicleId()).isEqualTo(pendingBudget.vehicleId());
        assertThat(result.items()).isEqualTo(pendingBudget.items());
        assertThat(result.totalAmount()).isEqualTo(pendingBudget.totalAmount());
        assertThat(result.createdAt()).isEqualTo(pendingBudget.createdAt());
    }

    // Helper methods

    private Budget createPendingBudget(String budgetId) {
        return Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of())
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now().minusHours(1))
            .build();
    }

    private Budget createApprovedBudget(String budgetId) {
        return Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of())
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.approved())
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private Budget createRejectedBudget(String budgetId) {
        return Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of())
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.rejected())
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
