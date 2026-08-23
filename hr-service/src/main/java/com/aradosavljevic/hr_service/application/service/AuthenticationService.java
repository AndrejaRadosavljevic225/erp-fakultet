package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.hr_service.application.dto.AuthResponse;
import com.aradosavljevic.hr_service.application.dto.CurrentUserDTO;
import com.aradosavljevic.hr_service.application.request.auth.ChangePasswordRequest;
import com.aradosavljevic.hr_service.application.request.auth.LoginRequest;
import com.aradosavljevic.hr_service.application.request.auth.RegisterRequest;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    CurrentUserDTO getCurrentUser(String username);
}
