package br.com.techchallenge.fiap.billingservice.infrastructure.orchestration;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.BudgetApprovedEvent;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.BudgetRejectedEvent;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher.SqsEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrator for publishing budget-related events.
 * Part of the Saga choreography pattern.
 * Publishes to:
 * - billing-events.fifo (FIFO, for Execution Service / audit)
 * - quote-approved-queue (Standard, for OS Service → IN_EXECUTION transition)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetEventOrchestrator {

    private final SqsEventPublisher eventPublisher;

    @Value("${aws.sqs.queues.quote-approved-queue:quote-approved-queue}")
    private String quoteApprovedQueueName;

    /**
     * Publish BudgetApprovedEvent to billing-events FIFO queue
     * and notify OS Service via quote-approved-queue.
     */
    public void publishBudgetApproved(Budget budget) {
        log.info("Publishing BudgetApprovedEvent for budget: {}", budget.budgetId());

        BudgetApprovedEvent event = BudgetApprovedEvent.builder()
                .eventType("BudgetApproved")
                .eventId(UUID.randomUUID().toString())
                .budgetId(budget.budgetId())
                .serviceOrderId(budget.serviceOrderId())
                .customerId(budget.customerId())
                .vehicleId(budget.vehicleId())
                .totalAmount(budget.totalAmount().value())
                .approvedAt(budget.updatedAt())
                .timestamp(LocalDateTime.now())
                .build();

        // Publish to billing-events.fifo (Execution Service / audit)
        eventPublisher.publishEvent(event);

        // Publish to quote-approved-queue (OS Service: WAITING_APPROVAL → IN_EXECUTION)
        try {
            Map<String, Object> osPayload = new HashMap<>();
            osPayload.put("orderId", Long.parseLong(budget.serviceOrderId()));
            osPayload.put("budgetId", budget.budgetId());
            osPayload.put("totalAmount", budget.totalAmount().value());
            osPayload.put("timestamp", LocalDateTime.now().toString());

            eventPublisher.publishToStandardQueue(quoteApprovedQueueName, osPayload);
            log.info("BudgetApprovedEvent published to quote-approved-queue for OS: {}", budget.serviceOrderId());
        } catch (Exception e) {
            log.error("Failed to publish to quote-approved-queue for OS: {}", budget.serviceOrderId(), e);
        }

        log.info("BudgetApprovedEvent published for budget: {}", budget.budgetId());
    }

    /**
     * Publish BudgetRejectedEvent.
     */
    public void publishBudgetRejected(Budget budget) {
        log.info("Publishing BudgetRejectedEvent for budget: {}", budget.budgetId());

        BudgetRejectedEvent event = BudgetRejectedEvent.builder()
                .eventType("BudgetRejected")
                .eventId(UUID.randomUUID().toString())
                .budgetId(budget.budgetId())
                .serviceOrderId(budget.serviceOrderId())
                .customerId(budget.customerId())
                .rejectedAt(budget.updatedAt())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);

        log.info("BudgetRejectedEvent published for budget: {}", budget.budgetId());
    }
}
