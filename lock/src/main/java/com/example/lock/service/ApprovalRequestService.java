package com.example.lock.service;

import com.example.lock.domain.approval.ApprovalStatus;
import com.example.lock.dto.approval.ApprovalDecisionRequest;
import com.example.lock.dto.approval.ApprovalRequestCreateRequest;
import com.example.lock.dto.approval.ApprovalRequestResponse;
import java.util.List;

public interface ApprovalRequestService {

    ApprovalRequestResponse create(ApprovalRequestCreateRequest request);

    ApprovalRequestResponse approve(Long approvalRequestId, ApprovalDecisionRequest request);

    ApprovalRequestResponse reject(Long approvalRequestId, ApprovalDecisionRequest request);

    ApprovalRequestResponse getById(Long id);

    List<ApprovalRequestResponse> getAll(ApprovalStatus status);
}
