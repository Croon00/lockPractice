package com.example.lock.dto.approval;

import jakarta.validation.constraints.NotNull;

public record ApprovalDecisionRequest(
        @NotNull
        Long actorId,
        String comment
) {
}
