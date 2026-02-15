package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.mapper.BudgetDynamoMapper;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model.BudgetDynamoModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DynamoDB implementation of BudgetGateway.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class BudgetDynamoDBRepository implements BudgetGateway {

    private final DynamoDbClient dynamoDbClient;
    private final BudgetDynamoMapper mapper;

    @Value("${aws.dynamodb.tables.budgets}")
    private String tableName;

    @Override
    public Budget save(Budget budget) {
        log.info("Saving budget: {}", budget.budgetId());
        
        BudgetDynamoModel model = mapper.toModel(budget);
        Map<String, AttributeValue> item = mapper.toAttributeMap(model);

        PutItemRequest request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build();

        dynamoDbClient.putItem(request);
        
        log.info("Budget saved successfully: {}", budget.budgetId());
        return budget;
    }

    @Override
    public Budget update(Budget budget) {
        log.info("Updating budget: {}", budget.budgetId());
        return save(budget);
    }

    @Override
    public Optional<Budget> findById(String budgetId) {
        log.info("Finding budget by id: {}", budgetId);

        Map<String, AttributeValue> key = Map.of(
            "budgetId", AttributeValue.builder().s(budgetId).build()
        );

        GetItemRequest request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (!response.hasItem()) {
            log.info("Budget not found: {}", budgetId);
            return Optional.empty();
        }

        BudgetDynamoModel model = mapper.fromAttributeMap(response.item());
        Budget budget = mapper.toDomain(model);
        
        log.info("Budget found: {}", budgetId);
        return Optional.of(budget);
    }

    @Override
    public List<Budget> findByServiceOrderId(String serviceOrderId) {
        log.info("Finding budgets by service order: {}", serviceOrderId);

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

        List<Budget> budgets = response.items().stream()
            .map(mapper::fromAttributeMap)
            .map(mapper::toDomain)
            .collect(Collectors.toList());

        log.info("Found {} budgets for service order: {}", budgets.size(), serviceOrderId);
        return budgets;
    }

    @Override
    public List<Budget> findAll(int page, int size) {
        log.info("Finding all budgets - page: {}, size: {}", page, size);

        ScanRequest request = ScanRequest.builder()
            .tableName(tableName)
            .limit(size)
            .build();

        ScanResponse response = dynamoDbClient.scan(request);

        List<Budget> budgets = response.items().stream()
            .map(mapper::fromAttributeMap)
            .map(mapper::toDomain)
            .collect(Collectors.toList());

        log.info("Found {} budgets", budgets.size());
        return budgets;
    }

    @Override
    public long count() {
        log.info("Counting all budgets");

        ScanRequest request = ScanRequest.builder()
            .tableName(tableName)
            .select(Select.COUNT)
            .build();

        ScanResponse response = dynamoDbClient.scan(request);
        long count = response.count();

        log.info("Total budgets: {}", count);
        return count;
    }

    @Override
    public void deleteById(String budgetId) {
        log.info("Deleting budget: {}", budgetId);

        Map<String, AttributeValue> key = Map.of(
            "budgetId", AttributeValue.builder().s(budgetId).build()
        );

        DeleteItemRequest request = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();

        dynamoDbClient.deleteItem(request);
        
        log.info("Budget deleted: {}", budgetId);
    }
}
