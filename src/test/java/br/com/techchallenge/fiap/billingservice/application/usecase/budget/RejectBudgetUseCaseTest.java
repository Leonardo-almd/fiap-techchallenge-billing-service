package br.com.techchallenge.fiap.billingservice.application.usecase.budget;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItem;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetStatus;
import br.com.techchallenge.fiap.billingservice.application.entity.Price;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejectBudgetUseCase - Unit Tests")
class RejectBudgetUseCaseTest {

    @Mock
    private BudgetGateway budgetGateway;

    private RejectBudgetUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RejectBudgetUseCase(budgetGateway);
    }

    @Test
    @DisplayName("Should reject budget when status is pending")
    void shouldRejectBudgetWhenStatusIsPending() {
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget result = useCase.execute(budgetId);

        assertThat(result).isNotNull();
        assertThat(result.status().isRejected()).isTrue();
        assertThat(result.updatedAt()).isAfter(pendingBudget.updatedAt());
        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when budget does not exist")
    void shouldThrowNotFoundExceptionWhenBudgetDoesNotExist() {
        String budgetId = "NON-EXISTENT";
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(budgetId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Budget not found");

        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway, never()).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when budget is already approved")
    void shouldThrowExceptionWhenBudgetIsAlreadyApproved() {
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));

        assertThatThrownBy(() -> useCase.execute(budgetId))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("not pending approval");

        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway, never()).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when budget is already rejected")
    void shouldThrowExceptionWhenBudgetIsAlreadyRejected() {
        String budgetId = "BUDGET-001";
        Budget rejectedBudget = createRejectedBudget(budgetId);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(rejectedBudget));

        assertThatThrownBy(() -> useCase.execute(budgetId))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("not pending approval");

        verify(budgetGateway).findById(budgetId);
        verify(budgetGateway, never()).update(any(Budget.class));
    }

    @Test
    @DisplayName("Should call update gateway with rejected budget")
    void shouldCallUpdateGatewayWithRejectedBudget() {
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);
        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(budgetId);

        verify(budgetGateway).update(captor.capture());
        Budget updated = captor.getValue();
        assertThat(updated.status().isRejected()).isTrue();
        assertThat(updated.budgetId()).isEqualTo(budgetId);
    }

    @Test
    @DisplayName("Should trigger callback after rejection")
    void shouldTriggerCallbackAfterRejection() {
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Budget> captured = new ArrayList<>();
        useCase.setOnRejectionCallback(captured::add);

        useCase.execute(budgetId);

        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).budgetId()).isEqualTo(budgetId);
    }

    @Test
    @DisplayName("Should not trigger callback if not set")
    void shouldNotTriggerCallbackIfNotSet() {
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));
        when(budgetGateway.update(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget result = useCase.execute(budgetId);

        assertThat(result).isNotNull();
        assertThat(result.status().isRejected()).isTrue();
    }

    private BudgetItem defaultBudgetItem() {
        return new BudgetItem("item-1", BudgetItemType.SERVICE, "SVC-001", "Test item", 1,
            new Price(new BigDecimal("100.00")), new Price(new BigDecimal("100.00")));
    }

    private Budget createPendingBudget(String budgetId) {
        return Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of(defaultBudgetItem()))
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
            .items(List.of(defaultBudgetItem()))
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
            .items(List.of(defaultBudgetItem()))
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.rejected())
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
