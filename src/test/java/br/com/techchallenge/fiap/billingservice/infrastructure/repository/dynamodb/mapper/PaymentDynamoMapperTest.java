package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.mapper;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.entity.Price;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model.PaymentDynamoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentDynamoMapper - Unit Tests")
class PaymentDynamoMapperTest {

    private PaymentDynamoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentDynamoMapper();
    }

    @Test
    @DisplayName("Should convert Payment to DynamoDB model")
    void shouldConvertPaymentToModel() {
        Payment payment = createPayment("PAY-001", "BUDGET-001");

        PaymentDynamoModel model = mapper.toModel(payment);

        assertThat(model).isNotNull();
        assertThat(model.paymentId()).isEqualTo("PAY-001");
        assertThat(model.budgetId()).isEqualTo("BUDGET-001");
        assertThat(model.serviceOrderId()).isEqualTo("ORDER-001");
        assertThat(model.amount()).isEqualByComparingTo(payment.amount().value());
        assertThat(model.methodCode()).isEqualTo(payment.method().name());
        assertThat(model.statusCode()).isEqualTo(payment.status().name());
    }

    @Test
    @DisplayName("Should convert DynamoDB model to Payment domain")
    void shouldConvertModelToPayment() {
        Payment original = createPayment("PAY-001", "BUDGET-001");
        PaymentDynamoModel model = mapper.toModel(original);

        Payment result = mapper.toDomain(model);

        assertThat(result).isNotNull();
        assertThat(result.paymentId()).isEqualTo(original.paymentId());
        assertThat(result.budgetId()).isEqualTo(original.budgetId());
        assertThat(result.serviceOrderId()).isEqualTo(original.serviceOrderId());
        assertThat(result.amount().value()).isEqualByComparingTo(original.amount().value());
        assertThat(result.method()).isEqualTo(original.method());
        assertThat(result.status().name()).isEqualTo(original.status().name());
    }

    @Test
    @DisplayName("Should convert model to AttributeValue map")
    void shouldConvertModelToAttributeMap() {
        PaymentDynamoModel model = PaymentDynamoModel.builder()
            .paymentId("PAY-001")
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .amount(new BigDecimal("100.00"))
            .methodCode("PIX")
            .methodName("PIX")
            .statusCode("PROCESSING")
            .statusName("PROCESSING")
            .createdAt(LocalDateTime.now())
            .build();

        Map<String, AttributeValue> map = mapper.toAttributeMap(model);

        assertThat(map).containsKeys("paymentId", "budgetId", "serviceOrderId", "amount",
            "methodCode", "methodName", "statusCode", "statusName", "createdAt");
        assertThat(map.get("paymentId").s()).isEqualTo("PAY-001");
        assertThat(map.get("budgetId").s()).isEqualTo("BUDGET-001");
    }

    @Test
    @DisplayName("Should include optional fields in attribute map when present")
    void shouldIncludeOptionalFieldsInAttributeMapWhenPresent() {
        PaymentDynamoModel model = PaymentDynamoModel.builder()
            .paymentId("PAY-001")
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .amount(new BigDecimal("100.00"))
            .methodCode("PIX")
            .methodName("PIX")
            .statusCode("PAID")
            .statusName("PAID")
            .externalId("EXT-123")
            .authorizationCode("AUTH-456")
            .failureReason(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Map<String, AttributeValue> map = mapper.toAttributeMap(model);

        assertThat(map).containsKey("externalId");
        assertThat(map).containsKey("authorizationCode");
        assertThat(map).containsKey("updatedAt");
    }

    @Test
    @DisplayName("Should convert AttributeValue map to model")
    void shouldConvertAttributeMapToModel() {
        LocalDateTime now = LocalDateTime.now();
        PaymentDynamoModel original = PaymentDynamoModel.builder()
            .paymentId("PAY-001")
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .amount(new BigDecimal("100.00"))
            .methodCode("PIX")
            .methodName("PIX")
            .statusCode("PAID")
            .statusName("PAID")
            .externalId("EXT-123")
            .createdAt(now)
            .updatedAt(now)
            .build();
        Map<String, AttributeValue> map = mapper.toAttributeMap(original);

        PaymentDynamoModel result = mapper.fromAttributeMap(map);

        assertThat(result).isNotNull();
        assertThat(result.paymentId()).isEqualTo(original.paymentId());
        assertThat(result.budgetId()).isEqualTo(original.budgetId());
        assertThat(result.amount()).isEqualByComparingTo(original.amount());
    }

    @Test
    @DisplayName("Should handle round-trip conversion correctly")
    void shouldHandleRoundTripConversion() {
        Payment original = createPayment("PAY-001", "BUDGET-001");

        PaymentDynamoModel model = mapper.toModel(original);
        Map<String, AttributeValue> map = mapper.toAttributeMap(model);
        PaymentDynamoModel modelFromMap = mapper.fromAttributeMap(map);
        Payment result = mapper.toDomain(modelFromMap);

        assertThat(result.paymentId()).isEqualTo(original.paymentId());
        assertThat(result.budgetId()).isEqualTo(original.budgetId());
        assertThat(result.serviceOrderId()).isEqualTo(original.serviceOrderId());
        assertThat(result.amount().value()).isEqualByComparingTo(original.amount().value());
    }

    private Payment createPayment(String paymentId, String budgetId) {
        return Payment.builder()
            .paymentId(paymentId)
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .amount(Price.of("100.00").value())
            .method(PaymentMethod.PIX)
            .status(PaymentStatus.pending())
            .createdAt(LocalDateTime.now())
            .build();
    }
}
