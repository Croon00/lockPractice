package com.example.lock.dto.user;

import com.example.lock.domain.user.AppUser;
import com.example.lock.domain.user.UserRole;
import java.time.LocalDateTime;

public record AppUserResponse(
        Long id,
        String employeeNo,
        String name,
        UserRole role,
        Long departmentId,
        String departmentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AppUserResponse from(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getEmployeeNo(),
                user.getName(),
                user.getRole(),
                user.getDepartment().getId(),
                user.getDepartment().getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
