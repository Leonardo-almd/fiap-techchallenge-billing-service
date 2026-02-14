package br.com.techchallenge.fiap.billingservice.infrastructure.config;

import br.com.techchallenge.fiap.billingservice.application.controller.BudgetCleanArchController;
import br.com.techchallenge.fiap.billingservice.application.controller.PaymentCleanArchController;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.ApproveBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.CreateBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.FindBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.RejectBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.FindPaymentUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.ProcessPaymentUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.RefundPaymentUseCase;
import br.com.techchallenge.fiap.billingservice.infrastructure.orchestration.BudgetEventOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Bean configuration for Clean Architecture components.
 */
@Configuration
public class BeanConfig {

    // Budget Use Cases

    @Bean
    public CreateBudgetUseCase createBudgetUseCase(BudgetGateway budgetGateway) {
        return new CreateBudgetUseCase(budgetGateway);
    }

    @Bean
    public FindBudgetUseCase findBudgetUseCase(BudgetGateway budgetGateway) {
        return new FindBudgetUseCase(budgetGateway);
    }

    @Bean
    public ApproveBudgetUseCase approveBudgetUseCase(
            BudgetGateway budgetGateway,
            BudgetEventOrchestrator budgetEventOrchestrator) {
        ApproveBudgetUseCase useCase = new ApproveBudgetUseCase(budgetGateway);
        useCase.setOnApprovalCallback(budgetEventOrchestrator::publishBudgetApproved);
        return useCase;
    }

    @Bean
    public RejectBudgetUseCase rejectBudgetUseCase(
            BudgetGateway budgetGateway,
            BudgetEventOrchestrator budgetEventOrchestrator) {
        RejectBudgetUseCase useCase = new RejectBudgetUseCase(budgetGateway);
        useCase.setOnRejectionCallback(budgetEventOrchestrator::publishBudgetRejected);
        return useCase;
    }

    // Payment Use Cases

    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(
            BudgetGateway budgetGateway,
            PaymentGateway paymentGateway) {
        return new ProcessPaymentUseCase(budgetGateway, paymentGateway);
    }

    @Bean
    public FindPaymentUseCase findPaymentUseCase(PaymentGateway paymentGateway) {
        return new FindPaymentUseCase(paymentGateway);
    }

    @Bean
    public RefundPaymentUseCase refundPaymentUseCase(PaymentGateway paymentGateway) {
        return new RefundPaymentUseCase(paymentGateway);
    }

    // Clean Architecture Controllers

    @Bean
    public BudgetCleanArchController budgetCleanArchController(
            CreateBudgetUseCase createBudgetUseCase,
            FindBudgetUseCase findBudgetUseCase,
            ApproveBudgetUseCase approveBudgetUseCase,
            RejectBudgetUseCase rejectBudgetUseCase) {
        return new BudgetCleanArchController(
            createBudgetUseCase,
            findBudgetUseCase,
            approveBudgetUseCase,
            rejectBudgetUseCase
        );
    }

    @Bean
    public PaymentCleanArchController paymentCleanArchController(
            ProcessPaymentUseCase processPaymentUseCase,
            FindPaymentUseCase findPaymentUseCase,
            RefundPaymentUseCase refundPaymentUseCase) {
        return new PaymentCleanArchController(
            processPaymentUseCase,
            findPaymentUseCase,
            refundPaymentUseCase
        );
    }
}
