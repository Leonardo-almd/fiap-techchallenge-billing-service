package br.com.techchallenge.fiap.billingservice.infrastructure.orchestration;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.BudgetApprovedEvent;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.BudgetRejectedEvent;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher.SqsEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrator for publishing budget-related events.
 * Part of the Saga choreography pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetEventOrchestrator {

    private final SqsEventPublisher eventPublisher;

    /**
     * Publish BudgetApprovedEvent.
     */
    public void publishBudgetApproved(Budget budget) {
        log.info("📤 Publishing BudgetApprovedEvent for budget: {}", budget.budgetId());

        BudgetApprovedEvent event = BudgetApprovedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .budgetId(budget.budgetId())
            .serviceOrderId(budget.serviceOrderId())
            .customerId(budget.customerId())
            .vehicleId(budget.vehicleId())
            .totalAmount(budget.totalAmount().value())
            .approvedAt(budget.updatedAt())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEvent(event);

        log.info("✅ BudgetApprovedEvent published for budget: {}", budget.budgetId());
    }

    /**
     * Publish BudgetRejectedEvent.
     */
    public void publishBudgetRejected(Budget budget) {
        log.info("📤 Publishing BudgetRejectedEvent for budget: {}", budget.budgetId());

        BudgetRejectedEvent event = BudgetRejectedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .budgetId(budget.budgetId())
            .serviceOrderId(budget.serviceOrderId())
            .customerId(budget.customerId())
            .rejectedAt(budget.updatedAt())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEvent(event);

        log.info("✅ BudgetRejectedEvent published for budget: {}", budget.budgetId());
    }
}
