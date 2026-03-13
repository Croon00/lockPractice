package com.example.lock.controller;

import com.example.lock.domain.approval.ApprovalStatus;
import com.example.lock.dto.approval.ApprovalDecisionRequest;
import com.example.lock.dto.approval.ApprovalRequestCreateRequest;
import com.example.lock.dto.approval.ApprovalRequestResponse;
import com.example.lock.service.ApprovalRequestService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approval-requests")
public class ApprovalRequestController {

    private final ApprovalRequestService approvalRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApprovalRequestResponse create(@Valid @RequestBody ApprovalRequestCreateRequest request) {
        return approvalRequestService.create(request);
    }

    @PostMapping("/{id}/approve")
    public ApprovalRequestResponse approve(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        return approvalRequestService.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    public ApprovalRequestResponse reject(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        return approvalRequestService.reject(id, request);
    }

    @GetMapping("/{id}")
    public ApprovalRequestResponse getById(@PathVariable Long id) {
        return approvalRequestService.getById(id);
    }

    @GetMapping
    public List<ApprovalRequestResponse> getAll(@RequestParam(required = false) ApprovalStatus status) {
        return approvalRequestService.getAll(status);
    }
}
