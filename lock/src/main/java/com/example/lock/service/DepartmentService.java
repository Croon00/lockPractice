package com.example.lock.service;

import com.example.lock.dto.department.DepartmentCreateRequest;
import com.example.lock.dto.department.DepartmentResponse;
import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(DepartmentCreateRequest request);

    List<DepartmentResponse> getAll();

    DepartmentResponse getById(Long id);
}
