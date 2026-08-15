package com.aradosavljevic.hr_service.application.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username je obavezan")
    private String username;

    @NotBlank(message = "Lozinka je obavezna")
    private String password;
}
