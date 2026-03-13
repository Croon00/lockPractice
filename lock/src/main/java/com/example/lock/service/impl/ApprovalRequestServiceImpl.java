package com.example.lock.service.impl;

import com.example.lock.domain.approval.ApprovalAction;
import com.example.lock.domain.approval.ApprovalHistory;
import com.example.lock.domain.approval.ApprovalRequest;
import com.example.lock.domain.approval.ApprovalStatus;
import com.example.lock.domain.budget.Budget;
import com.example.lock.domain.department.Department;
import com.example.lock.domain.user.AppUser;
import com.example.lock.dto.approval.ApprovalDecisionRequest;
import com.example.lock.dto.approval.ApprovalHistoryResponse;
import com.example.lock.dto.approval.ApprovalRequestCreateRequest;
import com.example.lock.dto.approval.ApprovalRequestResponse;
import com.example.lock.exception.BadRequestException;
import com.example.lock.exception.NotFoundException;
import com.example.lock.repository.AppUserRepository;
import com.example.lock.repository.ApprovalHistoryRepository;
import com.example.lock.repository.ApprovalRequestRepository;
import com.example.lock.repository.BudgetRepository;
import com.example.lock.repository.DepartmentRepository;
import com.example.lock.service.ApprovalRequestService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalRequestServiceImpl implements ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final AppUserRepository appUserRepository;
    private final BudgetRepository budgetRepository;

    @Override
    @Transactional
    public ApprovalRequestResponse create(ApprovalRequestCreateRequest request) {
        approvalRequestRepository.findByRequestNo(request.requestNo())
                .ifPresent(existing -> {
                    throw new BadRequestException("이미 존재하는 요청 번호입니다.");
                });

        Department department = findDepartment(request.departmentId());
        AppUser requester = findUser(request.requesterId());
        AppUser approver = request.approverId() == null ? null : findUser(request.approverId());

        validateSameDepartment(department, requester, "요청자");
        if (approver != null) {
            validateSameDepartment(department, approver, "승인자");
        }

        ApprovalRequest approvalRequest = approvalRequestRepository.save(ApprovalRequest.builder()
                .requestNo(request.requestNo())
                .department(department)
                .requester(requester)
                .approver(approver)
                .title(request.title())
                .description(request.description())
                .amount(request.amount())
                .status(ApprovalStatus.READY)
                .requestedAt(LocalDateTime.now())
                .build());

        approvalHistoryRepository.save(ApprovalHistory.builder()
                .approvalRequest(approvalRequest)
                .actor(requester)
                .action(ApprovalAction.CREATE)
                .fromStatus(null)
                .toStatus(ApprovalStatus.READY)
                .comment("결재 요청 생성")
                .createdAt(LocalDateTime.now())
                .build());

        return getById(approvalRequest.getId());
    }

    @Override
    @Transactional
    public ApprovalRequestResponse approve(Long approvalRequestId, ApprovalDecisionRequest request) {
        ApprovalRequest approvalRequest = findApprovalRequest(approvalRequestId);
        AppUser actor = findUser(request.actorId());

        validateActor(approvalRequest, actor);

        Budget budget = budgetRepository.findForUpdateByDepartmentId(approvalRequest.getDepartment().getId())
                .orElseThrow(() -> new NotFoundException(
                        "예산을 찾을 수 없습니다. departmentId=" + approvalRequest.getDepartment().getId()));

        ApprovalStatus fromStatus = approvalRequest.getStatus();
        budget.deduct(approvalRequest.getAmount());
        approvalRequest.approve();

        approvalHistoryRepository.save(ApprovalHistory.builder()
                .approvalRequest(approvalRequest)
                .actor(actor)
                .action(ApprovalAction.APPROVE)
                .fromStatus(fromStatus)
                .toStatus(ApprovalStatus.APPROVED)
                .comment(request.comment())
                .createdAt(LocalDateTime.now())
                .build());

        return getById(approvalRequestId);
    }

    @Override
    @Transactional
    public ApprovalRequestResponse reject(Long approvalRequestId, ApprovalDecisionRequest request) {
        ApprovalRequest approvalRequest = findApprovalRequest(approvalRequestId);
        AppUser actor = findUser(request.actorId());

        validateActor(approvalRequest, actor);

        ApprovalStatus fromStatus = approvalRequest.getStatus();
        approvalRequest.reject();

        approvalHistoryRepository.save(ApprovalHistory.builder()
                .approvalRequest(approvalRequest)
                .actor(actor)
                .action(ApprovalAction.REJECT)
                .fromStatus(fromStatus)
                .toStatus(ApprovalStatus.REJECTED)
                .comment(request.comment())
                .createdAt(LocalDateTime.now())
                .build());

        return getById(approvalRequestId);
    }

    @Override
    public ApprovalRequestResponse getById(Long id) {
        ApprovalRequest approvalRequest = findApprovalRequest(id);
        return toResponse(approvalRequest);
    }

    @Override
    public List<ApprovalRequestResponse> getAll(ApprovalStatus status) {
        List<ApprovalRequest> requests = status == null
                ? approvalRequestRepository.findAll()
                : approvalRequestRepository.findByStatus(status);

        return requests.stream()
                .map(this::toResponse)
                .toList();
    }

    private ApprovalRequestResponse toResponse(ApprovalRequest approvalRequest) {
        List<ApprovalHistoryResponse> histories = approvalHistoryRepository
                .findByApprovalRequestIdOrderByCreatedAtAsc(approvalRequest.getId())
                .stream()
                .map(ApprovalHistoryResponse::from)
                .toList();
        return ApprovalRequestResponse.from(approvalRequest, histories);
    }

    private ApprovalRequest findApprovalRequest(Long id) {
        return approvalRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("결재 요청을 찾을 수 없습니다. id=" + id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("부서를 찾을 수 없습니다. id=" + id));
    }

    private AppUser findUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id=" + id));
    }

    private void validateSameDepartment(Department department, AppUser user, String roleName) {
        if (!department.getId().equals(user.getDepartment().getId())) {
            throw new BadRequestException(roleName + "의 부서가 결재 요청 부서와 다릅니다.");
        }
    }

    private void validateActor(ApprovalRequest approvalRequest, AppUser actor) {
        if (approvalRequest.getApprover() == null) {
            throw new BadRequestException("승인자가 지정되지 않은 요청입니다.");
        }
        if (!approvalRequest.getApprover().getId().equals(actor.getId())) {
            throw new BadRequestException("지정된 승인자만 처리할 수 있습니다.");
        }
    }
}
