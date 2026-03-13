package com.example.lock.dto.user;

import com.example.lock.domain.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppUserCreateRequest(
        @NotBlank
        @Size(max = 50)
        String employeeNo,
        @NotBlank
        @Size(max = 100)
        String name,
        @NotNull
        UserRole role,
        @NotNull
        Long departmentId
) {
}
