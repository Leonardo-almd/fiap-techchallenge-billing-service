package br.com.techchallenge.fiap.billingservice.infrastructure.controller;

import br.com.techchallenge.fiap.billingservice.application.controller.BudgetCleanArchController;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetRequestDto;
import br.com.techchallenge.fiap.billingservice.application.dto.ErrorMessageDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Budget operations.
 */
@Tag(name = "Budgets", description = "Budget management endpoints")
@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetCleanArchController budgetController;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new budget", description = "Creates a new budget for a service order")
    @ApiResponse(responseCode = "201", description = "Budget created")
    @ApiResponse(responseCode = "400", description = "Invalid input data", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping
    public ResponseEntity<BudgetDto> create(@Valid @RequestBody BudgetRequestDto request) {
        BudgetDto budget = budgetController.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get budget by ID", description = "Returns a budget by its ID")
    @ApiResponse(responseCode = "200", description = "Budget found")
    @ApiResponse(responseCode = "404", description = "Budget not found", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetDto> findById(
            @Parameter(description = "Budget ID", required = true) @PathVariable String budgetId) {
        BudgetDto budget = budgetController.findById(budgetId);
        return ResponseEntity.ok(budget);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get budgets by service order", description = "Returns all budgets for a service order")
    @ApiResponse(responseCode = "200", description = "Budgets found")
    @GetMapping("/service-order/{serviceOrderId}")
    public ResponseEntity<List<BudgetDto>> findByServiceOrderId(
            @Parameter(description = "Service Order ID", required = true) @PathVariable String serviceOrderId) {
        List<BudgetDto> budgets = budgetController.findByServiceOrderId(serviceOrderId);
        return ResponseEntity.ok(budgets);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all budgets", description = "Returns a paginated list of budgets")
    @ApiResponse(responseCode = "200", description = "List of budgets")
    @GetMapping
    public ResponseEntity<PageDto<BudgetDto>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        PageDto<BudgetDto> budgets = budgetController.findAll(page, size);
        return ResponseEntity.ok(budgets);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve budget", description = "Approves a budget (extracted from monolith)")
    @ApiResponse(responseCode = "200", description = "Budget approved")
    @ApiResponse(responseCode = "400", description = "Budget is not pending approval", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Budget not found", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{budgetId}/approve")
    public ResponseEntity<BudgetDto> approve(
            @Parameter(description = "Budget ID", required = true) @PathVariable String budgetId) {
        BudgetDto budget = budgetController.approve(budgetId);
        return ResponseEntity.ok(budget);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject budget", description = "Rejects a budget")
    @ApiResponse(responseCode = "200", description = "Budget rejected")
    @ApiResponse(responseCode = "400", description = "Budget is not pending approval", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Budget not found", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{budgetId}/reject")
    public ResponseEntity<BudgetDto> reject(
            @Parameter(description = "Budget ID", required = true) @PathVariable String budgetId) {
        BudgetDto budget = budgetController.reject(budgetId);
        return ResponseEntity.ok(budget);
    }
}
