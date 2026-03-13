package com.example.lock.dto.department;

import com.example.lock.domain.department.Department;
import java.time.LocalDateTime;

public record DepartmentResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
