package com.example.lock.controller;

import com.example.lock.dto.user.AppUserCreateRequest;
import com.example.lock.dto.user.AppUserResponse;
import com.example.lock.service.AppUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse create(@Valid @RequestBody AppUserCreateRequest request) {
        return appUserService.create(request);
    }

    @GetMapping
    public List<AppUserResponse> getAll() {
        return appUserService.getAll();
    }

    @GetMapping("/{id}")
    public AppUserResponse getById(@PathVariable Long id) {
        return appUserService.getById(id);
    }
}
