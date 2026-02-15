package br.com.techchallenge.fiap.billingservice.application.controller;

import br.com.techchallenge.fiap.billingservice.application.dto.BudgetDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetItemRequestDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetRequestDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PageDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItem;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetStatus;
import br.com.techchallenge.fiap.billingservice.application.entity.Price;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.ApproveBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.CreateBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.FindBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.RejectBudgetUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetCleanArchController - Unit Tests")
class BudgetCleanArchControllerTest {

    @Mock
    private CreateBudgetUseCase createBudgetUseCase;
    @Mock
    private FindBudgetUseCase findBudgetUseCase;
    @Mock
    private ApproveBudgetUseCase approveBudgetUseCase;
    @Mock
    private RejectBudgetUseCase rejectBudgetUseCase;

    private BudgetCleanArchController controller;

    @BeforeEach
    void setUp() {
        controller = new BudgetCleanArchController(
            createBudgetUseCase, findBudgetUseCase, approveBudgetUseCase, rejectBudgetUseCase
        );
    }

    @Test
    @DisplayName("Should create budget and return DTO")
    void shouldCreateBudgetAndReturnDto() {
        BudgetRequestDto request = new BudgetRequestDto(
            "ORDER-001", "CUST-001", "VEH-001",
            List.of(new BudgetItemRequestDto(BudgetItemType.SERVICE, "SVC-1", "Oil change", 1, new BigDecimal("100.00")))
        );
        Budget saved = createBudget("BUDGET-001");
        when(createBudgetUseCase.execute(eq("ORDER-001"), eq("CUST-001"), eq("VEH-001"), any())).thenReturn(saved);

        BudgetDto result = controller.create(request);

        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo("BUDGET-001");
        verify(createBudgetUseCase).execute(eq("ORDER-001"), eq("CUST-001"), eq("VEH-001"), any());
    }

    @Test
    @DisplayName("Should find by id and return DTO")
    void shouldFindByIdAndReturnDto() {
        Budget budget = createBudget("BUDGET-001");
        when(findBudgetUseCase.findById("BUDGET-001")).thenReturn(budget);

        BudgetDto result = controller.findById("BUDGET-001");

        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo("BUDGET-001");
        verify(findBudgetUseCase).findById("BUDGET-001");
    }

    @Test
    @DisplayName("Should find by service order id and return list")
    void shouldFindByServiceOrderIdAndReturnList() {
        List<Budget> budgets = List.of(createBudget("B1"), createBudget("B2"));
        when(findBudgetUseCase.findByServiceOrderId("ORDER-001")).thenReturn(budgets);

        List<BudgetDto> result = controller.findByServiceOrderId("ORDER-001");

        assertThat(result).hasSize(2);
        verify(findBudgetUseCase).findByServiceOrderId("ORDER-001");
    }

    @Test
    @DisplayName("Should find all paginated and return PageDto")
    void shouldFindAllPaginatedAndReturnPageDto() {
        List<Budget> budgets = List.of(createBudget("B1"));
        when(findBudgetUseCase.findAll(0, 10)).thenReturn(budgets);
        when(findBudgetUseCase.count()).thenReturn(1L);

        PageDto<BudgetDto> result = controller.findAll(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        verify(findBudgetUseCase).findAll(0, 10);
        verify(findBudgetUseCase).count();
    }

    @Test
    @DisplayName("Should approve and return DTO")
    void shouldApproveAndReturnDto() {
        Budget approved = createApprovedBudget("BUDGET-001");
        when(approveBudgetUseCase.execute("BUDGET-001")).thenReturn(approved);

        BudgetDto result = controller.approve("BUDGET-001");

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("APPROVED");
        verify(approveBudgetUseCase).execute("BUDGET-001");
    }

    @Test
    @DisplayName("Should reject and return DTO")
    void shouldRejectAndReturnDto() {
        Budget rejected = createRejectedBudget("BUDGET-001");
        when(rejectBudgetUseCase.execute("BUDGET-001")).thenReturn(rejected);

        BudgetDto result = controller.reject("BUDGET-001");

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("REJECTED");
        verify(rejectBudgetUseCase).execute("BUDGET-001");
    }

    private BudgetItem defaultItem() {
        return new BudgetItem("item-1", BudgetItemType.SERVICE, "SVC-001", "Item", 1,
            new Price(new BigDecimal("100.00")), new Price(new BigDecimal("100.00")));
    }

    private Budget createBudget(String id) {
        return Budget.builder()
            .budgetId(id)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of(defaultItem()))
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private Budget createApprovedBudget(String id) {
        return Budget.builder()
            .budgetId(id)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of(defaultItem()))
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.approved())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private Budget createRejectedBudget(String id) {
        return Budget.builder()
            .budgetId(id)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of(defaultItem()))
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.rejected())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
