package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.hr_service.application.dto.AuthResponse;
import com.aradosavljevic.hr_service.application.dto.CurrentUserDTO;
import com.aradosavljevic.hr_service.application.request.auth.ChangePasswordRequest;
import com.aradosavljevic.hr_service.application.request.auth.LoginRequest;
import com.aradosavljevic.hr_service.application.request.auth.RegisterRequest;
import com.aradosavljevic.hr_service.application.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registracija uspesna", authenticationService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Prijava uspesna", authenticationService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserDTO> me(Authentication authentication) {
        return ApiResponse.success(authenticationService.getCurrentUser(authentication.getName()));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        authenticationService.changePassword(authentication.getName(), request);
        return ApiResponse.success("Lozinka je promenjena", null);
    }
}
