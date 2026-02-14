package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a monetary value.
 * Immutable and with business validations.
 */
public record Price(BigDecimal value) {

    public Price {
        if (Objects.isNull(value)) {
            throw new InvalidDataException("Price value must not be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Price value must not be negative");
        }
    }

    public Price add(Price other) {
        return new Price(this.value.add(other.value));
    }

    public Price subtract(Price other) {
        return new Price(this.value.subtract(other.value));
    }

    public Price multiply(int quantity) {
        return new Price(this.value.multiply(BigDecimal.valueOf(quantity)));
    }

    public static Price zero() {
        return new Price(BigDecimal.ZERO);
    }

    public static Price of(String value) {
        return new Price(new BigDecimal(value));
    }

    public static Price of(double value) {
        return new Price(BigDecimal.valueOf(value));
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isGreaterThan(Price other) {
        return value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(Price other) {
        return value.compareTo(other.value) < 0;
    }
}
