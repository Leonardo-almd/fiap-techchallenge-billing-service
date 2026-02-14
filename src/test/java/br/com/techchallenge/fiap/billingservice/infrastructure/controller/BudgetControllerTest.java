package br.com.techchallenge.fiap.billingservice.infrastructure.controller;

import br.com.techchallenge.fiap.billingservice.application.controller.BudgetCleanArchController;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetItemRequestDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetRequestDto;
import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@DisplayName("BudgetController - REST API Tests")
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetCleanArchController budgetController;

    @Test
    @DisplayName("POST /budgets - Should create budget successfully")
    void shouldCreateBudgetSuccessfully() throws Exception {
        // Arrange
        BudgetRequestDto request = createValidBudgetRequest();
        BudgetDto response = createBudgetDto("BUDGET-001");

        when(budgetController.create(any(BudgetRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.budgetId").value("BUDGET-001"))
            .andExpect(jsonPath("$.serviceOrderId").value("ORDER-001"))
            .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    @DisplayName("GET /budgets/{id} - Should return budget by ID")
    void shouldReturnBudgetById() throws Exception {
        // Arrange
        String budgetId = "BUDGET-001";
        BudgetDto response = createBudgetDto(budgetId);

        when(budgetController.findById(budgetId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/budgets/{budgetId}", budgetId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.budgetId").value(budgetId));
    }

    @Test
    @DisplayName("PUT /budgets/{id}/approve - Should approve budget")
    void shouldApproveBudget() throws Exception {
        // Arrange
        String budgetId = "BUDGET-001";
        BudgetDto response = createApprovedBudgetDto(budgetId);

        when(budgetController.approve(budgetId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/budgets/{budgetId}/approve", budgetId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.budgetId").value(budgetId))
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PUT /budgets/{id}/reject - Should reject budget")
    void shouldRejectBudget() throws Exception {
        // Arrange
        String budgetId = "BUDGET-001";
        BudgetDto response = createRejectedBudgetDto(budgetId);

        when(budgetController.reject(budgetId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/budgets/{budgetId}/reject", budgetId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.budgetId").value(budgetId))
            .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    // Helper methods

    private BudgetRequestDto createValidBudgetRequest() {
        BudgetItemRequestDto item = new BudgetItemRequestDto(
            BudgetItemType.SERVICE,
            "SVC-001",
            "Oil Change",
            1,
            new BigDecimal("100.00")
        );

        return new BudgetRequestDto(
            "ORDER-001",
            "CUST-001",
            "VEH-001",
            List.of(item)
        );
    }

    private BudgetDto createBudgetDto(String budgetId) {
        return new BudgetDto(
            budgetId,
            "ORDER-001",
            "CUST-001",
            "VEH-001",
            List.of(),
            new BigDecimal("100.00"),
            "PENDING_APPROVAL",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    private BudgetDto createApprovedBudgetDto(String budgetId) {
        return new BudgetDto(
            budgetId,
            "ORDER-001",
            "CUST-001",
            "VEH-001",
            List.of(),
            new BigDecimal("100.00"),
            "APPROVED",
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        );
    }

    private BudgetDto createRejectedBudgetDto(String budgetId) {
        return new BudgetDto(
            budgetId,
            "ORDER-001",
            "CUST-001",
            "VEH-001",
            List.of(),
            new BigDecimal("100.00"),
            "REJECTED",
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        );
    }
}
