package com.example.lock.repository;

import com.example.lock.domain.approval.ApprovalRequest;
import com.example.lock.domain.approval.ApprovalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    Optional<ApprovalRequest> findByRequestNo(String requestNo);

    @EntityGraph(attributePaths = {"department", "requester", "approver"})
    List<ApprovalRequest> findByStatus(ApprovalStatus status);

    @EntityGraph(attributePaths = {"department", "requester", "approver"})
    List<ApprovalRequest> findAll();

    @Override
    @EntityGraph(attributePaths = {"department", "requester", "approver"})
    Optional<ApprovalRequest> findById(Long id);
}
