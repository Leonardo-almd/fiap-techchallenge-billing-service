package br.com.techchallenge.fiap.billingservice.infrastructure.controller;

import br.com.techchallenge.fiap.billingservice.application.controller.PaymentCleanArchController;
import br.com.techchallenge.fiap.billingservice.application.dto.ErrorMessageDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PageDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PaymentDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PaymentRequestDto;
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
 * REST controller for Payment operations.
 */
@Tag(name = "Payments", description = "Payment processing endpoints")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentCleanArchController paymentController;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Process payment", description = "Processes a payment for an approved budget")
    @ApiResponse(responseCode = "201", description = "Payment created and processing")
    @ApiResponse(responseCode = "400", description = "Invalid input data or budget not approved", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Budget not found", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping
    public ResponseEntity<PaymentDto> processPayment(@Valid @RequestBody PaymentRequestDto request) {
        PaymentDto payment = paymentController.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payment by ID", description = "Returns a payment by its ID")
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not found", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> findById(
            @Parameter(description = "Payment ID", required = true) @PathVariable String paymentId) {
        PaymentDto payment = paymentController.findById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payment by budget", description = "Returns a payment for a specific budget")
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not found", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/budget/{budgetId}")
    public ResponseEntity<PaymentDto> findByBudgetId(
            @Parameter(description = "Budget ID", required = true) @PathVariable String budgetId) {
        PaymentDto payment = paymentController.findByBudgetId(budgetId);
        return ResponseEntity.ok(payment);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payments by service order", description = "Returns all payments for a service order")
    @ApiResponse(responseCode = "200", description = "Payments found")
    @GetMapping("/service-order/{serviceOrderId}")
    public ResponseEntity<List<PaymentDto>> findByServiceOrderId(
            @Parameter(description = "Service Order ID", required = true) @PathVariable String serviceOrderId) {
        List<PaymentDto> payments = paymentController.findByServiceOrderId(serviceOrderId);
        return ResponseEntity.ok(payments);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all payments", description = "Returns a paginated list of payments")
    @ApiResponse(responseCode = "200", description = "List of payments")
    @GetMapping
    public ResponseEntity<PageDto<PaymentDto>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        PageDto<PaymentDto> payments = paymentController.findAll(page, size);
        return ResponseEntity.ok(payments);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Refund payment", description = "Refunds a payment (Saga compensation)")
    @ApiResponse(responseCode = "200", description = "Payment refunded")
    @ApiResponse(responseCode = "400", description = "Payment is not paid", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Payment not found", 
                 content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentDto> refund(
            @Parameter(description = "Payment ID", required = true) @PathVariable String paymentId) {
        PaymentDto payment = paymentController.refund(paymentId);
        return ResponseEntity.ok(payment);
    }
}
