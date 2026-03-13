package com.example.lock.repository;

import com.example.lock.domain.approval.ApprovalHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    List<ApprovalHistory> findByApprovalRequestIdOrderByCreatedAtAsc(Long approvalRequestId);
}
