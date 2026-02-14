package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.mapper;

import br.com.techchallenge.fiap.billingservice.application.entity.*;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model.PaymentDynamoModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper for converting between Payment domain entity and DynamoDB model.
 */
@Component
@Slf4j
public class PaymentDynamoMapper {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convert Payment domain entity to DynamoDB model.
     */
    public PaymentDynamoModel toModel(Payment payment) {
        return PaymentDynamoModel.builder()
            .paymentId(payment.paymentId())
            .budgetId(payment.budgetId())
            .serviceOrderId(payment.serviceOrderId())
            .amount(payment.amount().value())
            .methodCode(payment.method().name())
            .methodName(payment.method().name())
            .statusCode(payment.status().name())
            .statusName(payment.status().name())
            .externalId(payment.externalId())
            .authorizationCode(payment.authorizationCode())
            .failureReason(payment.failureReason())
            .createdAt(payment.createdAt())
            .updatedAt(payment.updatedAt())
            .build();
    }

    /**
     * Convert DynamoDB model to Payment domain entity.
     */
    public Payment toDomain(PaymentDynamoModel model) {
        PaymentMethod method = PaymentMethod.valueOf(model.methodCode());
        PaymentStatus status = new PaymentStatus(PaymentStatusEnum.valueOf(model.statusCode()));

        return Payment.builder()
            .paymentId(model.paymentId())
            .budgetId(model.budgetId())
            .serviceOrderId(model.serviceOrderId())
            .amount(model.amount())
            .method(method)
            .status(status)
            .externalId(model.externalId())
            .authorizationCode(model.authorizationCode())
            .failureReason(model.failureReason())
            .createdAt(model.createdAt())
            .updatedAt(model.updatedAt())
            .build();
    }

    /**
     * Convert DynamoDB model to AttributeValue map (for put/save operations).
     */
    public Map<String, AttributeValue> toAttributeMap(PaymentDynamoModel model) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("paymentId", AttributeValue.builder().s(model.paymentId()).build());
        item.put("budgetId", AttributeValue.builder().s(model.budgetId()).build());
        item.put("serviceOrderId", AttributeValue.builder().s(model.serviceOrderId()).build());
        item.put("amount", AttributeValue.builder().n(model.amount().toString()).build());
        item.put("methodCode", AttributeValue.builder().s(model.methodCode()).build());
        item.put("methodName", AttributeValue.builder().s(model.methodName()).build());
        item.put("statusCode", AttributeValue.builder().s(model.statusCode()).build());
        item.put("statusName", AttributeValue.builder().s(model.statusName()).build());
        item.put("createdAt", AttributeValue.builder().s(model.createdAt().format(DATETIME_FORMATTER)).build());

        // Optional fields
        if (model.externalId() != null) {
            item.put("externalId", AttributeValue.builder().s(model.externalId()).build());
        }
        if (model.authorizationCode() != null) {
            item.put("authorizationCode", AttributeValue.builder().s(model.authorizationCode()).build());
        }
        if (model.failureReason() != null) {
            item.put("failureReason", AttributeValue.builder().s(model.failureReason()).build());
        }
        if (model.updatedAt() != null) {
            item.put("updatedAt", AttributeValue.builder().s(model.updatedAt().format(DATETIME_FORMATTER)).build());
        }

        return item;
    }

    /**
     * Convert AttributeValue map to DynamoDB model (for get/query operations).
     */
    public PaymentDynamoModel fromAttributeMap(Map<String, AttributeValue> item) {
        return PaymentDynamoModel.builder()
            .paymentId(item.get("paymentId").s())
            .budgetId(item.get("budgetId").s())
            .serviceOrderId(item.get("serviceOrderId").s())
            .amount(new BigDecimal(item.get("amount").n()))
            .methodCode(item.get("methodCode").s())
            .methodName(item.get("methodName").s())
            .statusCode(item.get("statusCode").s())
            .statusName(item.get("statusName").s())
            .externalId(item.containsKey("externalId") ? item.get("externalId").s() : null)
            .authorizationCode(item.containsKey("authorizationCode") ? item.get("authorizationCode").s() : null)
            .failureReason(item.containsKey("failureReason") ? item.get("failureReason").s() : null)
            .createdAt(LocalDateTime.parse(item.get("createdAt").s(), DATETIME_FORMATTER))
            .updatedAt(item.containsKey("updatedAt") ? 
                LocalDateTime.parse(item.get("updatedAt").s(), DATETIME_FORMATTER) : null)
            .build();
    }
}
