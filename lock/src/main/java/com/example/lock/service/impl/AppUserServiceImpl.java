package com.example.lock.service.impl;

import com.example.lock.domain.department.Department;
import com.example.lock.domain.user.AppUser;
import com.example.lock.dto.user.AppUserCreateRequest;
import com.example.lock.dto.user.AppUserResponse;
import com.example.lock.exception.BadRequestException;
import com.example.lock.exception.NotFoundException;
import com.example.lock.repository.AppUserRepository;
import com.example.lock.repository.DepartmentRepository;
import com.example.lock.service.AppUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public AppUserResponse create(AppUserCreateRequest request) {
        appUserRepository.findByEmployeeNo(request.employeeNo())
                .ifPresent(user -> {
                    throw new BadRequestException("이미 존재하는 사번입니다.");
                });

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("부서를 찾을 수 없습니다. id=" + request.departmentId()));

        AppUser appUser = appUserRepository.save(AppUser.builder()
                .employeeNo(request.employeeNo())
                .name(request.name())
                .role(request.role())
                .department(department)
                .build());

        return AppUserResponse.from(appUser);
    }

    @Override
    public List<AppUserResponse> getAll() {
        return appUserRepository.findAll().stream()
                .map(AppUserResponse::from)
                .toList();
    }

    @Override
    public AppUserResponse getById(Long id) {
        return AppUserResponse.from(appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id=" + id)));
    }
}
