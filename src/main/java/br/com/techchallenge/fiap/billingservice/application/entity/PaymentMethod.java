package br.com.techchallenge.fiap.billingservice.application.entity;

/**
 * Payment method enumeration.
 */
public enum PaymentMethod {
    CREDIT_CARD,  // Cartão de crédito
    DEBIT_CARD,   // Cartão de débito
    PIX,          // PIX
    BOLETO,       // Boleto bancário
    CASH          // Dinheiro
}
