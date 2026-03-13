package com.example.lock.dto.approval;

import com.example.lock.domain.approval.ApprovalAction;
import com.example.lock.domain.approval.ApprovalHistory;
import com.example.lock.domain.approval.ApprovalStatus;
import java.time.LocalDateTime;

public record ApprovalHistoryResponse(
        Long id,
        Long actorId,
        String actorName,
        ApprovalAction action,
        ApprovalStatus fromStatus,
        ApprovalStatus toStatus,
        String comment,
        LocalDateTime createdAt
) {

    public static ApprovalHistoryResponse from(ApprovalHistory history) {
        return new ApprovalHistoryResponse(
                history.getId(),
                history.getActor().getId(),
                history.getActor().getName(),
                history.getAction(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getComment(),
                history.getCreatedAt()
        );
    }
}
