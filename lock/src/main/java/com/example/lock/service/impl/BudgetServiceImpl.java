package com.example.lock.service.impl;

import com.example.lock.domain.budget.Budget;
import com.example.lock.domain.department.Department;
import com.example.lock.dto.budget.BudgetResponse;
import com.example.lock.dto.budget.BudgetUpsertRequest;
import com.example.lock.exception.BadRequestException;
import com.example.lock.exception.NotFoundException;
import com.example.lock.repository.BudgetRepository;
import com.example.lock.repository.DepartmentRepository;
import com.example.lock.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public BudgetResponse upsert(BudgetUpsertRequest request) {
        validateAmounts(request.totalAmount(), request.remainingAmount());

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("부서를 찾을 수 없습니다. id=" + request.departmentId()));

        Budget budget = budgetRepository.findByDepartmentId(request.departmentId())
                .map(existing -> {
                    existing.updateAmounts(request.totalAmount(), request.remainingAmount());
                    return existing;
                })
                .orElseGet(() -> Budget.builder()
                        .department(department)
                        .totalAmount(request.totalAmount())
                        .remainingAmount(request.remainingAmount())
                        .build());

        return BudgetResponse.from(budgetRepository.save(budget));
    }

    @Override
    public BudgetResponse getByDepartmentId(Long departmentId) {
        return BudgetResponse.from(budgetRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new NotFoundException("예산을 찾을 수 없습니다. departmentId=" + departmentId)));
    }

    private void validateAmounts(Long totalAmount, Long remainingAmount) {
        if (remainingAmount > totalAmount) {
            throw new BadRequestException("잔여 예산은 총 예산보다 클 수 없습니다.");
        }
    }
}
