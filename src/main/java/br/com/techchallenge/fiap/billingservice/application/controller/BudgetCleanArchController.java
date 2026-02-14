package br.com.techchallenge.fiap.billingservice.application.controller;

import br.com.techchallenge.fiap.billingservice.application.dto.BudgetDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetItemRequestDto;
import br.com.techchallenge.fiap.billingservice.application.dto.BudgetRequestDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PageDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.presenter.BudgetPresenter;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.ApproveBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.CreateBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.FindBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.RejectBudgetUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clean Architecture controller for Budget operations.
 * This is the application layer controller (port) that coordinates use cases.
 */
@RequiredArgsConstructor
public class BudgetCleanArchController {

    private final CreateBudgetUseCase createBudgetUseCase;
    private final FindBudgetUseCase findBudgetUseCase;
    private final ApproveBudgetUseCase approveBudgetUseCase;
    private final RejectBudgetUseCase rejectBudgetUseCase;

    /**
     * Create a new budget.
     */
    public BudgetDto create(BudgetRequestDto request) {
        List<CreateBudgetUseCase.BudgetItemRequest> items = request.items().stream()
            .map(item -> new CreateBudgetUseCase.BudgetItemRequest(
                item.type(),
                item.itemCode(),
                item.description(),
                item.quantity(),
                item.unitPrice()
            ))
            .collect(Collectors.toList());

        Budget budget = createBudgetUseCase.execute(
            request.serviceOrderId(),
            request.customerId(),
            request.vehicleId(),
            items
        );

        return BudgetPresenter.toDto(budget);
    }

    /**
     * Find budget by ID.
     */
    public BudgetDto findById(String budgetId) {
        Budget budget = findBudgetUseCase.findById(budgetId);
        return BudgetPresenter.toDto(budget);
    }

    /**
     * Find budgets by service order ID.
     */
    public List<BudgetDto> findByServiceOrderId(String serviceOrderId) {
        List<Budget> budgets = findBudgetUseCase.findByServiceOrderId(serviceOrderId);
        return BudgetPresenter.toDtoList(budgets);
    }

    /**
     * Find all budgets (paginated).
     */
    public PageDto<BudgetDto> findAll(int page, int size) {
        List<Budget> budgets = findBudgetUseCase.findAll(page, size);
        long total = findBudgetUseCase.count();
        
        List<BudgetDto> budgetDtos = BudgetPresenter.toDtoList(budgets);
        
        return PageDto.of(budgetDtos, page, size, total);
    }

    /**
     * Approve a budget.
     */
    public BudgetDto approve(String budgetId) {
        Budget approvedBudget = approveBudgetUseCase.execute(budgetId);
        return BudgetPresenter.toDto(approvedBudget);
    }

    /**
     * Reject a budget.
     */
    public BudgetDto reject(String budgetId) {
        Budget rejectedBudget = rejectBudgetUseCase.execute(budgetId);
        return BudgetPresenter.toDto(rejectedBudget);
    }
}
