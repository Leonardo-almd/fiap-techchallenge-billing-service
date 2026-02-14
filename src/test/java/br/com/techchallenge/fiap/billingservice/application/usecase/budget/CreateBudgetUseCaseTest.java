package br.com.techchallenge.fiap.billingservice.application.usecase.budget;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBudgetUseCase - Unit Tests")
class CreateBudgetUseCaseTest {

    @Mock
    private BudgetGateway budgetGateway;

    private CreateBudgetUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBudgetUseCase(budgetGateway);
    }

    @Test
    @DisplayName("Should create budget with valid data")
    void shouldCreateBudgetWithValidData() {
        // Arrange
        String serviceOrderId = "ORDER-001";
        String customerId = "CUST-001";
        String vehicleId = "VEH-001";

        List<CreateBudgetUseCase.BudgetItemRequest> items = Arrays.asList(
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-001",
                "Troca de óleo",
                1,
                new BigDecimal("150.00")
            ),
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.RESOURCE,
                "RES-001",
                "Óleo 5W30",
                4,
                new BigDecimal("25.00")
            )
        );

        when(budgetGateway.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute(serviceOrderId, customerId, vehicleId, items);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.vehicleId()).isEqualTo(vehicleId);
        assertThat(result.items()).hasSize(2);
        assertThat(result.status().isPendingApproval()).isTrue();
        
        // Total = 150 + (4 * 25) = 250
        assertThat(result.totalAmount().value()).isEqualByComparingTo(new BigDecimal("250.00"));

        verify(budgetGateway, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should generate unique budget ID")
    void shouldGenerateUniqueBudgetId() {
        // Arrange
        List<CreateBudgetUseCase.BudgetItemRequest> items = List.of(
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-001",
                "Service",
                1,
                new BigDecimal("100.00")
            )
        );

        when(budgetGateway.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute("ORDER-001", "CUST-001", "VEH-001", items);

        // Assert
        assertThat(result.budgetId()).isNotNull();
        assertThat(result.budgetId()).isNotBlank();
    }

    @Test
    @DisplayName("Should calculate total amount correctly with multiple items")
    void shouldCalculateTotalAmountCorrectly() {
        // Arrange
        List<CreateBudgetUseCase.BudgetItemRequest> items = Arrays.asList(
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-001",
                "Service 1",
                2,
                new BigDecimal("50.00")
            ),
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-002",
                "Service 2",
                1,
                new BigDecimal("100.00")
            ),
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.RESOURCE,
                "RES-001",
                "Resource 1",
                3,
                new BigDecimal("30.00")
            )
        );

        when(budgetGateway.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute("ORDER-001", "CUST-001", "VEH-001", items);

        // Assert
        // Total = (2 * 50) + (1 * 100) + (3 * 30) = 100 + 100 + 90 = 290
        assertThat(result.totalAmount().value()).isEqualByComparingTo(new BigDecimal("290.00"));
    }

    @Test
    @DisplayName("Should generate unique item IDs for each budget item")
    void shouldGenerateUniqueItemIds() {
        // Arrange
        List<CreateBudgetUseCase.BudgetItemRequest> items = Arrays.asList(
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-001",
                "Service 1",
                1,
                new BigDecimal("100.00")
            ),
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-002",
                "Service 2",
                1,
                new BigDecimal("200.00")
            )
        );

        when(budgetGateway.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute("ORDER-001", "CUST-001", "VEH-001", items);

        // Assert
        assertThat(result.items()).hasSize(2);
        String itemId1 = result.items().get(0).itemId();
        String itemId2 = result.items().get(1).itemId();
        
        assertThat(itemId1).isNotNull().isNotBlank();
        assertThat(itemId2).isNotNull().isNotBlank();
        assertThat(itemId1).isNotEqualTo(itemId2);
    }

    @Test
    @DisplayName("Should set timestamps correctly")
    void shouldSetTimestampsCorrectly() {
        // Arrange
        List<CreateBudgetUseCase.BudgetItemRequest> items = List.of(
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-001",
                "Service",
                1,
                new BigDecimal("100.00")
            )
        );

        when(budgetGateway.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute("ORDER-001", "CUST-001", "VEH-001", items);

        // Assert
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
        assertThat(result.createdAt()).isEqualTo(result.updatedAt());
    }

    @Test
    @DisplayName("Should throw exception when items list is null")
    void shouldThrowExceptionWhenItemsIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> 
            useCase.execute("ORDER-001", "CUST-001", "VEH-001", null)
        )
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("must have at least one item");

        verify(budgetGateway, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw exception when items list is empty")
    void shouldThrowExceptionWhenItemsIsEmpty() {
        // Act & Assert
        assertThatThrownBy(() -> 
            useCase.execute("ORDER-001", "CUST-001", "VEH-001", Collections.emptyList())
        )
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("must have at least one item");

        verify(budgetGateway, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should call gateway save with correct budget")
    void shouldCallGatewaySaveWithCorrectBudget() {
        // Arrange
        String serviceOrderId = "ORDER-001";
        String customerId = "CUST-001";
        String vehicleId = "VEH-001";

        List<CreateBudgetUseCase.BudgetItemRequest> items = List.of(
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-001",
                "Service",
                1,
                new BigDecimal("100.00")
            )
        );

        ArgumentCaptor<Budget> budgetCaptor = ArgumentCaptor.forClass(Budget.class);
        when(budgetGateway.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(serviceOrderId, customerId, vehicleId, items);

        // Assert
        verify(budgetGateway).save(budgetCaptor.capture());
        
        Budget capturedBudget = budgetCaptor.getValue();
        assertThat(capturedBudget.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(capturedBudget.customerId()).isEqualTo(customerId);
        assertThat(capturedBudget.vehicleId()).isEqualTo(vehicleId);
        assertThat(capturedBudget.status().isPendingApproval()).isTrue();
    }

    @Test
    @DisplayName("Should preserve item properties correctly")
    void shouldPreserveItemPropertiesCorrectly() {
        // Arrange
        List<CreateBudgetUseCase.BudgetItemRequest> items = List.of(
            new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "SVC-001",
                "Oil Change",
                2,
                new BigDecimal("75.50")
            )
        );

        when(budgetGateway.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Budget result = useCase.execute("ORDER-001", "CUST-001", "VEH-001", items);

        // Assert
        assertThat(result.items()).hasSize(1);
        var item = result.items().get(0);
        
        assertThat(item.type()).isEqualTo(BudgetItemType.SERVICE);
        assertThat(item.itemCode()).isEqualTo("SVC-001");
        assertThat(item.description()).isEqualTo("Oil Change");
        assertThat(item.quantity()).isEqualTo(2);
        assertThat(item.unitPrice().value()).isEqualByComparingTo(new BigDecimal("75.50"));
        assertThat(item.totalPrice().value()).isEqualByComparingTo(new BigDecimal("151.00"));
    }
}
