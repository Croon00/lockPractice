package com.example.lock.dto.approval;

import com.example.lock.domain.approval.ApprovalRequest;
import com.example.lock.domain.approval.ApprovalStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ApprovalRequestResponse(
        Long id,
        String requestNo,
        Long departmentId,
        String departmentName,
        Long requesterId,
        String requesterName,
        Long approverId,
        String approverName,
        String title,
        String description,
        Long amount,
        ApprovalStatus status,
        Long version,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ApprovalHistoryResponse> histories
) {

    public static ApprovalRequestResponse from(ApprovalRequest request, List<ApprovalHistoryResponse> histories) {
        return new ApprovalRequestResponse(
                request.getId(),
                request.getRequestNo(),
                request.getDepartment().getId(),
                request.getDepartment().getName(),
                request.getRequester().getId(),
                request.getRequester().getName(),
                request.getApprover() == null ? null : request.getApprover().getId(),
                request.getApprover() == null ? null : request.getApprover().getName(),
                request.getTitle(),
                request.getDescription(),
                request.getAmount(),
                request.getStatus(),
                request.getVersion(),
                request.getRequestedAt(),
                request.getApprovedAt(),
                request.getRejectedAt(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                histories
        );
    }
}
