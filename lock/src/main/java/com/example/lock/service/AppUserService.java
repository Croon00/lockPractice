package com.example.lock.service;

import com.example.lock.dto.user.AppUserCreateRequest;
import com.example.lock.dto.user.AppUserResponse;
import java.util.List;

public interface AppUserService {

    AppUserResponse create(AppUserCreateRequest request);

    List<AppUserResponse> getAll();

    AppUserResponse getById(Long id);
}
