package br.com.techchallenge.fiap.billingservice.application.entity;

/**
 * Payment status enumeration.
 */
public enum PaymentStatusEnum {
    PENDING,      // Aguardando processamento
    PROCESSING,   // Em processamento
    PAID,         // Pago com sucesso
    FAILED,       // Falha no pagamento
    REFUNDED      // Estornado (compensação)
}
