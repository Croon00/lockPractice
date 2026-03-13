package com.example.lock.dto.budget;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BudgetUpsertRequest(
        @NotNull
        Long departmentId,
        @NotNull
        @Min(0)
        Long totalAmount,
        @NotNull
        @Min(0)
        Long remainingAmount
) {
}
