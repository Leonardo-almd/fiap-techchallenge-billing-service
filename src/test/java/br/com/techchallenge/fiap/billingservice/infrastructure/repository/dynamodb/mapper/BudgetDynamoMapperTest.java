package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.mapper;

import br.com.techchallenge.fiap.billingservice.application.entity.*;
import br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model.BudgetDynamoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BudgetDynamoMapper - Unit Tests")
class BudgetDynamoMapperTest {

    private BudgetDynamoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BudgetDynamoMapper();
    }

    @Test
    @DisplayName("Should convert Budget to DynamoDB model")
    void shouldConvertBudgetToModel() {
        // Arrange
        Budget budget = createBudget();

        // Act
        BudgetDynamoModel model = mapper.toModel(budget);

        // Assert
        assertThat(model).isNotNull();
        assertThat(model.budgetId()).isEqualTo(budget.budgetId());
        assertThat(model.serviceOrderId()).isEqualTo(budget.serviceOrderId());
        assertThat(model.customerId()).isEqualTo(budget.customerId());
        assertThat(model.vehicleId()).isEqualTo(budget.vehicleId());
        assertThat(model.totalAmount()).isEqualByComparingTo(budget.totalAmount().value());
        assertThat(model.statusCode()).isEqualTo(budget.status().code());
        assertThat(model.statusName()).isEqualTo(budget.status().name());
        assertThat(model.items()).hasSize(budget.items().size());
    }

    @Test
    @DisplayName("Should convert DynamoDB model to Budget domain")
    void shouldConvertModelToBudget() {
        // Arrange
        Budget original = createBudget();
        BudgetDynamoModel model = mapper.toModel(original);

        // Act
        Budget result = mapper.toDomain(model);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo(original.budgetId());
        assertThat(result.serviceOrderId()).isEqualTo(original.serviceOrderId());
        assertThat(result.customerId()).isEqualTo(original.customerId());
        assertThat(result.vehicleId()).isEqualTo(original.vehicleId());
        assertThat(result.totalAmount().value()).isEqualByComparingTo(original.totalAmount().value());
        assertThat(result.status().code()).isEqualTo(original.status().code());
    }

    @Test
    @DisplayName("Should convert model to AttributeValue map")
    void shouldConvertModelToAttributeMap() {
        // Arrange
        Budget budget = createBudget();
        BudgetDynamoModel model = mapper.toModel(budget);

        // Act
        Map<String, AttributeValue> attributeMap = mapper.toAttributeMap(model);

        // Assert
        assertThat(attributeMap).isNotNull();
        assertThat(attributeMap).containsKeys(
            "budgetId", "serviceOrderId", "customerId", "vehicleId",
            "totalAmount", "statusCode", "statusName", "items",
            "createdAt", "updatedAt"
        );
        assertThat(attributeMap.get("budgetId").s()).isEqualTo(budget.budgetId());
        assertThat(attributeMap.get("serviceOrderId").s()).isEqualTo(budget.serviceOrderId());
    }

    @Test
    @DisplayName("Should convert AttributeValue map to model")
    void shouldConvertAttributeMapToModel() {
        // Arrange
        Budget budget = createBudget();
        BudgetDynamoModel originalModel = mapper.toModel(budget);
        Map<String, AttributeValue> attributeMap = mapper.toAttributeMap(originalModel);

        // Act
        BudgetDynamoModel result = mapper.fromAttributeMap(attributeMap);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo(originalModel.budgetId());
        assertThat(result.serviceOrderId()).isEqualTo(originalModel.serviceOrderId());
        assertThat(result.totalAmount()).isEqualByComparingTo(originalModel.totalAmount());
    }

    @Test
    @DisplayName("Should handle round-trip conversion correctly")
    void shouldHandleRoundTripConversion() {
        // Arrange
        Budget original = createBudget();

        // Act
        BudgetDynamoModel model = mapper.toModel(original);
        Map<String, AttributeValue> attributeMap = mapper.toAttributeMap(model);
        BudgetDynamoModel modelFromMap = mapper.fromAttributeMap(attributeMap);
        Budget result = mapper.toDomain(modelFromMap);

        // Assert
        assertThat(result.budgetId()).isEqualTo(original.budgetId());
        assertThat(result.serviceOrderId()).isEqualTo(original.serviceOrderId());
        assertThat(result.totalAmount().value()).isEqualByComparingTo(original.totalAmount().value());
        assertThat(result.items()).hasSize(original.items().size());
    }

    @Test
    @DisplayName("Should preserve budget items correctly")
    void shouldPreserveBudgetItemsCorrectly() {
        // Arrange
        Budget budget = createBudget();

        // Act
        BudgetDynamoModel model = mapper.toModel(budget);
        Budget result = mapper.toDomain(model);

        // Assert
        assertThat(result.items()).hasSize(1);
        BudgetItem originalItem = budget.items().get(0);
        BudgetItem resultItem = result.items().get(0);
        
        assertThat(resultItem.type()).isEqualTo(originalItem.type());
        assertThat(resultItem.itemCode()).isEqualTo(originalItem.itemCode());
        assertThat(resultItem.description()).isEqualTo(originalItem.description());
        assertThat(resultItem.quantity()).isEqualTo(originalItem.quantity());
        assertThat(resultItem.unitPrice().value()).isEqualByComparingTo(originalItem.unitPrice().value());
    }

    // Helper methods

    private Budget createBudget() {
        BudgetItem item = new BudgetItem(
            "ITEM-001",
            BudgetItemType.SERVICE,
            "SVC-001",
            "Oil Change",
            2,
            new Price(new BigDecimal("50.00")),
            new Price(new BigDecimal("100.00"))
        );

        return Budget.builder()
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of(item))
            .totalAmount(new BigDecimal("100.00"))
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
