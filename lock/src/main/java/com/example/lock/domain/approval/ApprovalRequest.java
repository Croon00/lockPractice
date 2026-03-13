package com.example.lock.domain.approval;

import com.example.lock.domain.common.BaseTimeEntity;
import com.example.lock.domain.department.Department;
import com.example.lock.domain.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "approval_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_no", nullable = false, unique = true, length = 50)
    private String requestNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private AppUser approver;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Builder
    public ApprovalRequest(
            String requestNo,
            Department department,
            AppUser requester,
            AppUser approver,
            String title,
            String description,
            Long amount,
            ApprovalStatus status,
            LocalDateTime requestedAt
    ) {
        this.requestNo = requestNo;
        this.department = department;
        this.requester = requester;
        this.approver = approver;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public void approve() {
        validateReady();
        this.status = ApprovalStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        validateReady();
        this.status = ApprovalStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }

    private void validateReady() {
        if (this.status != ApprovalStatus.READY) {
            throw new IllegalStateException("READY 상태에서만 처리할 수 있습니다.");
        }
    }
}
