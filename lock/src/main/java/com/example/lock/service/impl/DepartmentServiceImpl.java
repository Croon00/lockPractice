package com.example.lock.service.impl;

import com.example.lock.domain.department.Department;
import com.example.lock.dto.department.DepartmentCreateRequest;
import com.example.lock.dto.department.DepartmentResponse;
import com.example.lock.exception.BadRequestException;
import com.example.lock.exception.NotFoundException;
import com.example.lock.repository.DepartmentRepository;
import com.example.lock.service.DepartmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        departmentRepository.findByName(request.name())
                .ifPresent(department -> {
                    throw new BadRequestException("이미 존재하는 부서명입니다.");
                });

        Department department = departmentRepository.save(Department.builder()
                .name(request.name())
                .build());
        return DepartmentResponse.from(department);
    }

    @Override
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Override
    public DepartmentResponse getById(Long id) {
        return DepartmentResponse.from(findDepartment(id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("부서를 찾을 수 없습니다. id=" + id));
    }
}
