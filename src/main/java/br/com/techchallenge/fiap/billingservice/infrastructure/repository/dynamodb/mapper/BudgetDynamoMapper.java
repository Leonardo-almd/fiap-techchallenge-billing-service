package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.mapper;

import br.com.techchallenge.fiap.billingservice.application.entity.*;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model.BudgetDynamoModel;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model.BudgetItemDynamoModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Budget domain entity and DynamoDB model.
 */
@Component
@Slf4j
public class BudgetDynamoMapper {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final ObjectMapper objectMapper;

    public BudgetDynamoMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Convert Budget domain entity to DynamoDB model.
     */
    public BudgetDynamoModel toModel(Budget budget) {
        List<BudgetItemDynamoModel> itemModels = budget.items().stream()
            .map(this::toBudgetItemModel)
            .collect(Collectors.toList());

        return BudgetDynamoModel.builder()
            .budgetId(budget.budgetId())
            .serviceOrderId(budget.serviceOrderId())
            .customerId(budget.customerId())
            .vehicleId(budget.vehicleId())
            .items(itemModels)
            .totalAmount(budget.totalAmount().value())
            .statusCode(budget.status().name())
            .statusName(budget.status().name())
            .createdAt(budget.createdAt())
            .updatedAt(budget.updatedAt())
            .build();
    }

    /**
     * Convert BudgetItem to model.
     */
    private BudgetItemDynamoModel toBudgetItemModel(BudgetItem item) {
        return BudgetItemDynamoModel.builder()
            .itemId(item.itemId())
            .type(item.type().name())
            .itemCode(item.itemCode())
            .description(item.description())
            .quantity(item.quantity())
            .unitPrice(item.unitPrice().value())
            .totalPrice(item.totalPrice().value())
            .build();
    }

    /**
     * Convert DynamoDB model to Budget domain entity.
     */
    public Budget toDomain(BudgetDynamoModel model) {
        List<BudgetItem> items = model.items().stream()
            .map(this::toBudgetItemDomain)
            .collect(Collectors.toList());

        BudgetStatus status = new BudgetStatus(BudgetStatusEnum.valueOf(model.statusCode()));

        return Budget.builder()
            .budgetId(model.budgetId())
            .serviceOrderId(model.serviceOrderId())
            .customerId(model.customerId())
            .vehicleId(model.vehicleId())
            .items(items)
            .totalAmount(model.totalAmount())
            .status(status)
            .createdAt(model.createdAt())
            .updatedAt(model.updatedAt())
            .build();
    }

    /**
     * Convert model to BudgetItem domain.
     */
    private BudgetItem toBudgetItemDomain(BudgetItemDynamoModel model) {
        BudgetItemType type = BudgetItemType.valueOf(model.type());
        
        return new BudgetItem(
            model.itemId(),
            type,
            model.itemCode(),
            model.description(),
            model.quantity(),
            new Price(model.unitPrice()),
            new Price(model.totalPrice())
        );
    }

    /**
     * Convert DynamoDB model to AttributeValue map (for put/save operations).
     */
    public Map<String, AttributeValue> toAttributeMap(BudgetDynamoModel model) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("budgetId", AttributeValue.builder().s(model.budgetId()).build());
        item.put("serviceOrderId", AttributeValue.builder().s(model.serviceOrderId()).build());
        item.put("customerId", AttributeValue.builder().s(model.customerId()).build());
        item.put("vehicleId", AttributeValue.builder().s(model.vehicleId()).build());
        item.put("totalAmount", AttributeValue.builder().n(model.totalAmount().toString()).build());
        item.put("statusCode", AttributeValue.builder().s(model.statusCode()).build());
        item.put("statusName", AttributeValue.builder().s(model.statusName()).build());
        item.put("createdAt", AttributeValue.builder().s(model.createdAt().format(DATETIME_FORMATTER)).build());
        item.put("updatedAt", AttributeValue.builder().s(model.updatedAt().format(DATETIME_FORMATTER)).build());

        // Serialize items as JSON
        try {
            String itemsJson = objectMapper.writeValueAsString(model.items());
            item.put("items", AttributeValue.builder().s(itemsJson).build());
        } catch (JsonProcessingException e) {
            log.error("Error serializing budget items", e);
            throw new RuntimeException("Error serializing budget items", e);
        }

        return item;
    }

    /**
     * Convert AttributeValue map to DynamoDB model (for get/query operations).
     */
    public BudgetDynamoModel fromAttributeMap(Map<String, AttributeValue> item) {
        try {
            String itemsJson = item.get("items").s();
            List<BudgetItemDynamoModel> items = objectMapper.readValue(
                itemsJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, BudgetItemDynamoModel.class)
            );

            return BudgetDynamoModel.builder()
                .budgetId(item.get("budgetId").s())
                .serviceOrderId(item.get("serviceOrderId").s())
                .customerId(item.get("customerId").s())
                .vehicleId(item.get("vehicleId").s())
                .items(items)
                .totalAmount(new BigDecimal(item.get("totalAmount").n()))
                .statusCode(item.get("statusCode").s())
                .statusName(item.get("statusName").s())
                .createdAt(LocalDateTime.parse(item.get("createdAt").s(), DATETIME_FORMATTER))
                .updatedAt(LocalDateTime.parse(item.get("updatedAt").s(), DATETIME_FORMATTER))
                .build();
        } catch (JsonProcessingException e) {
            log.error("Error deserializing budget items", e);
            throw new RuntimeException("Error deserializing budget items", e);
        }
    }
}
