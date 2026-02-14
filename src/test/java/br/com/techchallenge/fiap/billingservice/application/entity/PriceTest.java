package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Price value object.
 */
class PriceTest {

    @Test
    void shouldCreateValidPrice() {
        // Given
        BigDecimal value = new BigDecimal("100.50");

        // When
        Price price = new Price(value);

        // Then
        assertThat(price.value()).isEqualByComparingTo(value);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // Given
        BigDecimal value = null;

        // When / Then
        assertThatThrownBy(() -> new Price(value))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("must not be null");
    }

    @Test
    void shouldThrowExceptionWhenValueIsNegative() {
        // Given
        BigDecimal value = new BigDecimal("-10.00");

        // When / Then
        assertThatThrownBy(() -> new Price(value))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("must not be negative");
    }

    @Test
    void shouldAddPrices() {
        // Given
        Price price1 = Price.of("50.00");
        Price price2 = Price.of("30.50");

        // When
        Price result = price1.add(price2);

        // Then
        assertThat(result.value()).isEqualByComparingTo("80.50");
    }

    @Test
    void shouldSubtractPrices() {
        // Given
        Price price1 = Price.of("100.00");
        Price price2 = Price.of("30.50");

        // When
        Price result = price1.subtract(price2);

        // Then
        assertThat(result.value()).isEqualByComparingTo("69.50");
    }

    @Test
    void shouldMultiplyPrice() {
        // Given
        Price price = Price.of("25.00");
        int quantity = 4;

        // When
        Price result = price.multiply(quantity);

        // Then
        assertThat(result.value()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldCreateZeroPrice() {
        // When
        Price price = Price.zero();

        // Then
        assertThat(price.value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(price.isZero()).isTrue();
    }

    @Test
    void shouldCompareGreaterThan() {
        // Given
        Price price1 = Price.of("100.00");
        Price price2 = Price.of("50.00");

        // When / Then
        assertThat(price1.isGreaterThan(price2)).isTrue();
        assertThat(price2.isGreaterThan(price1)).isFalse();
    }

    @Test
    void shouldCompareLessThan() {
        // Given
        Price price1 = Price.of("30.00");
        Price price2 = Price.of("100.00");

        // When / Then
        assertThat(price1.isLessThan(price2)).isTrue();
        assertThat(price2.isLessThan(price1)).isFalse();
    }

    @Test
    void shouldCreateFromString() {
        // When
        Price price = Price.of("123.45");

        // Then
        assertThat(price.value()).isEqualByComparingTo("123.45");
    }

    @Test
    void shouldCreateFromDouble() {
        // When
        Price price = Price.of(99.99);

        // Then
        assertThat(price.value()).isEqualByComparingTo("99.99");
    }
}
