package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Budget entity.
 */
class BudgetTest {

    @Test
    void shouldCreateValidBudget() {
        // Given
        String budgetId = "bdg-123";
        String serviceOrderId = "os-456";
        List<BudgetItem> items = createSampleItems();
        Price totalAmount = Budget.calculateTotalAmount(items);
        BudgetStatus status = BudgetStatus.pendingApproval();

        // When
        Budget budget = Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId(serviceOrderId)
            .customerId("cust-789")
            .vehicleId("veh-101")
            .items(items)
            .totalAmount(totalAmount.value())
            .status(status)
            .createdAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(budget.budgetId()).isEqualTo(budgetId);
        assertThat(budget.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(budget.status()).isEqualTo(status);
    }

    @Test
    void shouldThrowExceptionWhenServiceOrderIdIsNull() {
        // When / Then
        assertThatThrownBy(() -> Budget.builder()
            .budgetId("bdg-123")
            .serviceOrderId(null)
            .customerId("cust-789")
            .vehicleId("veh-101")
            .items(createSampleItems())
            .totalAmount(Price.of("100").value())
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now())
            .build())
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("serviceOrderId must not be null");
    }

    @Test
    void shouldThrowExceptionWhenItemsListIsEmpty() {
        // When / Then
        assertThatThrownBy(() -> Budget.builder()
            .budgetId("bdg-123")
            .serviceOrderId("os-456")
            .customerId("cust-789")
            .vehicleId("veh-101")
            .items(List.of())
            .totalAmount(Price.of("100").value())
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now())
            .build())
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("must have at least one item");
    }

    @Test
    void shouldUpdateStatusToApproved() {
        // Given
        Budget budget = createSampleBudget();
        LocalDateTime now = LocalDateTime.now();

        // When
        Budget approvedBudget = budget.withStatusUpdated(BudgetStatus.approved(), now);

        // Then
        assertThat(approvedBudget.status().isApproved()).isTrue();
        assertThat(approvedBudget.approvedAt()).isEqualTo(now);
        assertThat(approvedBudget.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateStatusToRejected() {
        // Given
        Budget budget = createSampleBudget();
        LocalDateTime now = LocalDateTime.now();

        // When
        Budget rejectedBudget = budget.withStatusUpdated(BudgetStatus.rejected(), now);

        // Then
        assertThat(rejectedBudget.status().isRejected()).isTrue();
        assertThat(rejectedBudget.rejectedAt()).isEqualTo(now);
        assertThat(rejectedBudget.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCalculateTotalAmountFromItems() {
        // Given
        List<BudgetItem> items = List.of(
            createBudgetItem("item-1", Price.of("150.00"), 1),
            createBudgetItem("item-2", Price.of("35.00"), 4)
        );

        // When
        Price total = Budget.calculateTotalAmount(items);

        // Then
        // 150.00 + (35.00 * 4) = 150.00 + 140.00 = 290.00
        assertThat(total.value()).isEqualByComparingTo("290.00");
    }

    @Test
    void shouldThrowExceptionWhenCalculatingTotalFromEmptyList() {
        // When / Then
        assertThatThrownBy(() -> Budget.calculateTotalAmount(List.of()))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("Cannot calculate total from empty items list");
    }

    @Test
    void shouldUpdateBudgetId() {
        // Given
        Budget budget = createSampleBudget();
        String newId = "bdg-new-999";

        // When
        Budget updatedBudget = budget.withId(newId);

        // Then
        assertThat(updatedBudget.budgetId()).isEqualTo(newId);
        assertThat(updatedBudget.serviceOrderId()).isEqualTo(budget.serviceOrderId());
    }

    // Helper methods

    private Budget createSampleBudget() {
        return Budget.builder()
            .budgetId("bdg-123")
            .serviceOrderId("os-456")
            .customerId("cust-789")
            .vehicleId("veh-101")
            .items(createSampleItems())
            .totalAmount(Price.of("290.00").value())
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now())
            .build();
    }

    private List<BudgetItem> createSampleItems() {
        return List.of(
            createBudgetItem("item-1", Price.of("150.00"), 1),
            createBudgetItem("item-2", Price.of("35.00"), 4)
        );
    }

    private BudgetItem createBudgetItem(String itemId, Price unitPrice, int quantity) {
        return new BudgetItem(
            itemId,
            BudgetItemType.SERVICE,
            "SVC-001",
            "Sample service",
            quantity,
            unitPrice,
            unitPrice.multiply(quantity)
        );
    }
}
