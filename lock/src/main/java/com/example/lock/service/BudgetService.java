package com.example.lock.service;

import com.example.lock.dto.budget.BudgetResponse;
import com.example.lock.dto.budget.BudgetUpsertRequest;

public interface BudgetService {

    BudgetResponse upsert(BudgetUpsertRequest request);

    BudgetResponse getByDepartmentId(Long departmentId);
}
