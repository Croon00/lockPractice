package com.example.lock.dto.approval;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApprovalRequestCreateRequest(
        @NotBlank
        @Size(max = 50)
        String requestNo,
        @NotNull
        Long departmentId,
        @NotNull
        Long requesterId,
        Long approverId,
        @NotBlank
        @Size(max = 200)
        String title,
        String description,
        @NotNull
        @Min(1)
        Long amount
) {
}
