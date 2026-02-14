package br.com.techchallenge.fiap.billingservice.application.entity;

/**
 * Budget status enumeration.
 */
public enum BudgetStatusEnum {
    PENDING_APPROVAL,  // Aguardando aprovação do cliente
    APPROVED,          // Aprovado - aciona pagamento
    REJECTED           // Rejeitado - rollback do Saga
}
