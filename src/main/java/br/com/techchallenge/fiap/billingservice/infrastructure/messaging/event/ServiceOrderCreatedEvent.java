package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event received from Service Order microservice when a new order is created.
 * This triggers budget creation in the Saga choreography.
 */
@Builder
public record ServiceOrderCreatedEvent(
    String eventId,
    String serviceOrderId,
    String customerId,
    String vehicleId,
    List<OrderItem> items,
    LocalDateTime timestamp
) {
    @Builder
    public record OrderItem(
        String type,
        String itemCode,
        String description,
        Integer quantity,
        java.math.BigDecimal unitPrice
    ) {}
}
