package br.com.techchallenge.fiap.billingservice.infrastructure.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.gateway.ExternalPaymentProvider;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentProviderRequest;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentProviderResult;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mercado Pago implementation of {@link ExternalPaymentProvider}.
 * Supports PIX and Boleto; card payments require a token from the frontend (not used in this flow).
 * @see <a href="https://www.mercadopago.com.br/developers/pt/docs">Mercado Pago Developers</a>
 */
@Slf4j
@RequiredArgsConstructor
public class MercadoPagoPaymentProvider implements ExternalPaymentProvider {

    private static final String PAYMENT_METHOD_PIX = "pix";
    private static final String PAYMENT_METHOD_BOLETO = "bolbradesco";

    private final String payerEmail;

    @Override
    public PaymentProviderResult processPayment(PaymentProviderRequest request) {
        String mpPaymentMethodId = mapPaymentMethodToMercadoPago(request.method());
        if (mpPaymentMethodId == null) {
            String msg = "Mercado Pago supports only PIX and BOLETO in this integration. Requested: "
                    + request.method();
            log.warn(msg);
            return PaymentProviderResult.failure(msg);
        }

        try {
            MercadoPagoConfig.setAccessToken(getAccessToken());
            PaymentClient client = new PaymentClient();

            PaymentCreateRequest createRequest = PaymentCreateRequest.builder()
                    .transactionAmount(request.amount())
                    .paymentMethodId(mpPaymentMethodId)
                    .description(request.description() != null ? request.description() : "Orçamento aprovado")
                    .externalReference(request.externalReference())
                    .payer(PaymentPayerRequest.builder().email(payerEmail).build())
                    .build();

            Map<String, String> headers = new HashMap<>();
            headers.put("X-Idempotency-Key", request.externalReference());

            MPRequestOptions options = MPRequestOptions.builder()
                    .customHeaders(headers)
                    .build();

            Payment payment = client.create(createRequest, options);

            String status = Objects.toString(payment.getStatus(), "").toUpperCase();
            Long id = payment.getId();
            String externalId = id != null ? String.valueOf(id) : null;
            String statusDetail = payment.getStatusDetail() != null ? payment.getStatusDetail() : status;

            switch (status) {
                case "APPROVED" -> {
                    log.info("Mercado Pago payment approved: id={}", externalId);
                    return PaymentProviderResult.success(externalId, statusDetail.isEmpty() ? "APPROVED" : statusDetail);
                }
                case "PENDING", "IN_PROCESS" -> {
                    log.info("Mercado Pago payment pending: id={}", externalId);
                    return PaymentProviderResult.success(externalId, statusDetail.isEmpty() ? "PENDING" : statusDetail);
                }
                case "REJECTED", "CANCELLED", "REFUNDED", "CHARGED_BACK" -> {
                    log.warn("Mercado Pago payment not approved: status={}, detail={}", status, statusDetail);
                    return PaymentProviderResult.failure(statusDetail);
                }
                default -> {
                    log.warn("Mercado Pago payment unknown status: {}", status);
                    return PaymentProviderResult.failure("Status desconhecido: " + status);
                }
            }
        } catch (MPApiException e) {
            String message = e.getApiResponse() != null && e.getApiResponse().getContent() != null
                    ? e.getApiResponse().getContent()
                    : e.getMessage();
            log.error("Mercado Pago API error: {}", message, e);
            return PaymentProviderResult.failure("Mercado Pago: " + message);
        } catch (MPException e) {
            log.error("Mercado Pago error", e);
            return PaymentProviderResult.failure("Mercado Pago: " + e.getMessage());
        }
    }

    private String getAccessToken() {
        String token = System.getenv("MERCADOPAGO_ACCESS_TOKEN");
        if (token == null || token.isBlank()) {
            token = System.getProperty("mercadopago.access-token");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "MERCADOPAGO_ACCESS_TOKEN (env) or mercadopago.access-token (system property) must be set");
        }
        return token;
    }

    private static String mapPaymentMethodToMercadoPago(PaymentMethod method) {
        return switch (method) {
            case PIX -> PAYMENT_METHOD_PIX;
            case BOLETO -> PAYMENT_METHOD_BOLETO;
            case CREDIT_CARD, DEBIT_CARD, CASH -> null;
        };
    }
}
