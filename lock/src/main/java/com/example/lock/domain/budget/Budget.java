package com.example.lock.domain.budget;

import com.example.lock.domain.common.BaseTimeEntity;
import com.example.lock.domain.department.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "budget",
        uniqueConstraints = @UniqueConstraint(name = "uk_budget_department", columnNames = "department_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "remaining_amount", nullable = false)
    private Long remainingAmount;

    @Version
    @Column(nullable = false)
    private Long version;

    @Builder
    public Budget(Department department, Long totalAmount, Long remainingAmount) {
        this.department = department;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
    }

    public void updateAmounts(Long totalAmount, Long remainingAmount) {
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
    }

    public void deduct(Long amount) {
        if (remainingAmount < amount) {
            throw new IllegalStateException("예산이 부족합니다.");
        }
        this.remainingAmount -= amount;
    }
}
