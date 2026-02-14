package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for BudgetItem entity.
 */
class BudgetItemTest {

    @Test
    void shouldCreateValidBudgetItem() {
        // Given
        String itemId = "item-123";
        BudgetItemType type = BudgetItemType.SERVICE;
        String itemCode = "SVC-001";
        String description = "Troca de óleo";
        Integer quantity = 1;
        Price unitPrice = Price.of("150.00");
        Price totalPrice = Price.of("150.00");

        // When
        BudgetItem item = new BudgetItem(
            itemId, type, itemCode, description, quantity, unitPrice, totalPrice
        );

        // Then
        assertThat(item.itemId()).isEqualTo(itemId);
        assertThat(item.type()).isEqualTo(type);
        assertThat(item.quantity()).isEqualTo(quantity);
        assertThat(item.totalPrice().value()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldThrowExceptionWhenItemIdIsNull() {
        // When / Then
        assertThatThrownBy(() -> new BudgetItem(
            null, BudgetItemType.SERVICE, "SVC-001", "Desc", 1, 
            Price.of("100"), Price.of("100")
        ))
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("itemId must not be null");
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        // When / Then
        assertThatThrownBy(() -> new BudgetItem(
            "item-123", null, "SVC-001", "Desc", 1,
            Price.of("100"), Price.of("100")
        ))
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("type must not be null");
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        // When / Then
        assertThatThrownBy(() -> new BudgetItem(
            "item-123", BudgetItemType.SERVICE, "SVC-001", "Desc", 0,
            Price.of("100"), Price.of("0")
        ))
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("quantity must be positive");
    }

    @Test
    void shouldThrowExceptionWhenTotalPriceDoesNotMatchCalculation() {
        // Given
        Price unitPrice = Price.of("50.00");
        int quantity = 4;
        Price wrongTotalPrice = Price.of("150.00"); // Should be 200.00

        // When / Then
        assertThatThrownBy(() -> new BudgetItem(
            "item-123", BudgetItemType.RESOURCE, "RES-001", "Peça", 
            quantity, unitPrice, wrongTotalPrice
        ))
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("totalPrice must equal unitPrice * quantity");
    }

    @Test
    void shouldCalculateTotalPriceCorrectly() {
        // Given
        Price unitPrice = Price.of("35.50");
        int quantity = 3;

        // When
        Price total = BudgetItem.calculateTotal(unitPrice, quantity);

        // Then
        assertThat(total.value()).isEqualByComparingTo("106.50");
    }

    @Test
    void shouldCreateResourceItem() {
        // Given
        Price unitPrice = Price.of("25.00");
        int quantity = 4;
        Price totalPrice = unitPrice.multiply(quantity);

        // When
        BudgetItem item = new BudgetItem(
            "item-456",
            BudgetItemType.RESOURCE,
            "RES-100",
            "Óleo 5W30",
            quantity,
            unitPrice,
            totalPrice
        );

        // Then
        assertThat(item.type()).isEqualTo(BudgetItemType.RESOURCE);
        assertThat(item.totalPrice().value()).isEqualByComparingTo("100.00");
    }
}
