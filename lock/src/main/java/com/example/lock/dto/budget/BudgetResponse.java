package com.example.lock.dto.budget;

import com.example.lock.domain.budget.Budget;
import java.time.LocalDateTime;

public record BudgetResponse(
        Long id,
        Long departmentId,
        String departmentName,
        Long totalAmount,
        Long remainingAmount,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getDepartment().getId(),
                budget.getDepartment().getName(),
                budget.getTotalAmount(),
                budget.getRemainingAmount(),
                budget.getVersion(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
