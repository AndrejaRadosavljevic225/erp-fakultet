package com.aradosavljevic.hr_service.application.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username je obavezan")
    @Size(min = 3, max = 50, message = "Username mora imati 3-50 karaktera")
    private String username;

    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 6, message = "Lozinka mora imati bar 6 karaktera")
    private String password;

    private Long workerId;

    private Long roleId;
}
