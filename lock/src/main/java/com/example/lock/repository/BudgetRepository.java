package com.example.lock.repository;

import com.example.lock.domain.budget.Budget;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByDepartmentId(Long departmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Budget b join fetch b.department where b.department.id = :departmentId")
    Optional<Budget> findForUpdateByDepartmentId(Long departmentId);
}
