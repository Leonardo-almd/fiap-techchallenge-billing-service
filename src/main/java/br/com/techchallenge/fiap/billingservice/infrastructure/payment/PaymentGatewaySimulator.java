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
    private final int minDelayMs;
    private final int maxDelayMs;

    // Configurable success rate (90% by default)
    private static final int SUCCESS_RATE = 90;

    /** Default constructor for Spring: 2–5 s delay. */
    public PaymentGatewaySimulator() {
        this(2000, 5000);
    }

    /**
     * Constructor for tests: set minDelayMs and maxDelayMs to 0 to disable delay.
     */
    public PaymentGatewaySimulator(int minDelayMs, int maxDelayMs) {
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
    }

    /**
     * Simulates payment processing.
     * 
     * Simulation rules:
     * - 90% success rate
     * - 10% failure rate
     * - Configurable delay (default 2-5 seconds; use 0,0 in tests for no delay)
     * 
     * @param request Payment request
     * @return Payment result with success/failure
     */
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("🔄 Simulating payment processing for amount: {} using method: {}", 
                 request.amount(), request.method());

        try {
            int delayMs = 0;
            if (maxDelayMs > 0) {
                int range = Math.max(0, maxDelayMs - minDelayMs);
                delayMs = minDelayMs + (range > 0 ? random.nextInt(range) : 0);
                Thread.sleep(delayMs);
            }

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
