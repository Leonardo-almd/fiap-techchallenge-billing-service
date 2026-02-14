package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.mapper.PaymentDynamoMapper;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model.PaymentDynamoModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DynamoDB implementation of PaymentGateway.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class PaymentDynamoDBRepository implements PaymentGateway {

    private final DynamoDbClient dynamoDbClient;
    private final PaymentDynamoMapper mapper;

    @Value("${aws.dynamodb.tables.payments}")
    private String tableName;

    @Override
    public Payment save(Payment payment) {
        log.info("Saving payment: {}", payment.paymentId());
        
        PaymentDynamoModel model = mapper.toModel(payment);
        Map<String, AttributeValue> item = mapper.toAttributeMap(model);

        PutItemRequest request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build();

        dynamoDbClient.putItem(request);
        
        log.info("Payment saved successfully: {}", payment.paymentId());
        return payment;
    }

    @Override
    public Payment update(Payment payment) {
        log.info("Updating payment: {}", payment.paymentId());
        return save(payment);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        log.info("Finding payment by id: {}", paymentId);

        Map<String, AttributeValue> key = Map.of(
            "paymentId", AttributeValue.builder().s(paymentId).build()
        );

        GetItemRequest request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (!response.hasItem()) {
            log.info("Payment not found: {}", paymentId);
            return Optional.empty();
        }

        PaymentDynamoModel model = mapper.fromAttributeMap(response.item());
        Payment payment = mapper.toDomain(model);
        
        log.info("Payment found: {}", paymentId);
        return Optional.of(payment);
    }

    @Override
    public Optional<Payment> findByBudgetId(String budgetId) {
        log.info("Finding payment by budget: {}", budgetId);

        Map<String, AttributeValue> expressionValues = Map.of(
            ":budgetId", AttributeValue.builder().s(budgetId).build()
        );

        QueryRequest request = QueryRequest.builder()
            .tableName(tableName)
            .indexName("BudgetIndex")
            .keyConditionExpression("budgetId = :budgetId")
            .expressionAttributeValues(expressionValues)
            .limit(1)
            .build();

        QueryResponse response = dynamoDbClient.query(request);

        if (response.items().isEmpty()) {
            log.info("Payment not found for budget: {}", budgetId);
            return Optional.empty();
        }

        PaymentDynamoModel model = mapper.fromAttributeMap(response.items().get(0));
        Payment payment = mapper.toDomain(model);
        
        log.info("Payment found for budget: {}", budgetId);
        return Optional.of(payment);
    }

    @Override
    public List<Payment> findByServiceOrderId(String serviceOrderId) {
        log.info("Finding payments by service order: {}", serviceOrderId);

        Map<String, AttributeValue> expressionValues = Map.of(
            ":serviceOrderId", AttributeValue.builder().s(serviceOrderId).build()
        );

        QueryRequest request = QueryRequest.builder()
            .tableName(tableName)
            .indexName("ServiceOrderIndex")
            .keyConditionExpression("serviceOrderId = :serviceOrderId")
            .expressionAttributeValues(expressionValues)
            .build();

        QueryResponse response = dynamoDbClient.query(request);

        List<Payment> payments = response.items().stream()
            .map(mapper::fromAttributeMap)
            .map(mapper::toDomain)
            .collect(Collectors.toList());

        log.info("Found {} payments for service order: {}", payments.size(), serviceOrderId);
        return payments;
    }

    @Override
    public List<Payment> findAll(int page, int size) {
        log.info("Finding all payments - page: {}, size: {}", page, size);

        ScanRequest request = ScanRequest.builder()
            .tableName(tableName)
            .limit(size)
            .build();

        ScanResponse response = dynamoDbClient.scan(request);

        List<Payment> payments = response.items().stream()
            .map(mapper::fromAttributeMap)
            .map(mapper::toDomain)
            .collect(Collectors.toList());

        log.info("Found {} payments", payments.size());
        return payments;
    }

    @Override
    public long count() {
        log.info("Counting all payments");

        ScanRequest request = ScanRequest.builder()
            .tableName(tableName)
            .select(Select.COUNT)
            .build();

        ScanResponse response = dynamoDbClient.scan(request);
        long count = response.count();

        log.info("Total payments: {}", count);
        return count;
    }

    @Override
    public void deleteById(String paymentId) {
        log.info("Deleting payment: {}", paymentId);

        Map<String, AttributeValue> key = Map.of(
            "paymentId", AttributeValue.builder().s(paymentId).build()
        );

        DeleteItemRequest request = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();

        dynamoDbClient.deleteItem(request);
        
        log.info("Payment deleted: {}", paymentId);
    }
}
