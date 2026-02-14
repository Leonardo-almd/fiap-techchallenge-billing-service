package br.com.techchallenge.fiap.billingservice.infrastructure.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Payment gateway simulator (illustrative - not a real payment integration).
 * Simulates payment processing with configurable success/failure rates.
 */
@Service
@Slf4j
public class PaymentGatewaySimulator {

    private final Random random = new Random();
    
    // Configurable success rate (90% by default)
    private static final int SUCCESS_RATE = 90;

    /**
     * Simulates payment processing.
     * 
     * Simulation rules:
     * - 90% success rate
     * - 10% failure rate
     * - 2-5 seconds delay to simulate processing
     * 
     * @param request Payment request
     * @return Payment result with success/failure
     */
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("🔄 Simulating payment processing for amount: {} using method: {}", 
                 request.amount(), request.method());

        try {
            // Simulate processing delay (2-5 seconds)
            int delayMs = random.nextInt(3000) + 2000;
            Thread.sleep(delayMs);

            // Simulate approval (90% chance)
            boolean approved = random.nextInt(100) < SUCCESS_RATE;

            if (approved) {
                String externalId = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String authCode = "AUTH-" + String.format("%06d", random.nextInt(999999));

                log.info("✅ Payment APPROVED - ExternalId: {}, AuthCode: {}, Delay: {}ms",
                         externalId, authCode, delayMs);

                return PaymentResult.success(externalId, authCode);
                
            } else {
                String failureReason = selectRandomFailureReason();
                log.warn("❌ Payment FAILED - Reason: {}", failureReason);

                return PaymentResult.failure(failureReason);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("⚠️  Payment processing interrupted", e);
            return PaymentResult.failure("Processing interrupted");
        }
    }

    /**
     * Select a random failure reason for failed payments.
     */
    private String selectRandomFailureReason() {
        String[] reasons = {
            "Cartão recusado",
            "Saldo insuficiente",
            "Cartão expirado",
            "Limite excedido",
            "Transação não autorizada",
            "Cartão bloqueado",
            "Dados do cartão inválidos"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    /**
     * Payment request data.
     */
    @Builder
    public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod method
    ) {}

    /**
     * Payment result from simulator.
     */
    @Builder
    public record PaymentResult(
        boolean success,
        String externalId,
        String authorizationCode,
        String failureReason
    ) {
        public static PaymentResult success(String externalId, String authCode) {
            return new PaymentResult(true, externalId, authCode, null);
        }

        public static PaymentResult failure(String reason) {
            return new PaymentResult(false, null, null, reason);
        }
    }
}
