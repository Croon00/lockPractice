package com.example.lock.controller;

import com.example.lock.dto.budget.BudgetResponse;
import com.example.lock.dto.budget.BudgetUpsertRequest;
import com.example.lock.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.upsert(request);
    }

    @PutMapping
    public BudgetResponse update(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.upsert(request);
    }

    @GetMapping("/department/{departmentId}")
    public BudgetResponse getByDepartmentId(@PathVariable Long departmentId) {
        return budgetService.getByDepartmentId(departmentId);
    }
}
