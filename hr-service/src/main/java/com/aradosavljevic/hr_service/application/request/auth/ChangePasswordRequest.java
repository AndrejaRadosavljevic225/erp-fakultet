package com.aradosavljevic.hr_service.application.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Stara lozinka je obavezna")
    private String oldPassword;

    @NotBlank(message = "Nova lozinka je obavezna")
    @Size(min = 6, message = "Nova lozinka mora imati bar 6 karaktera")
    private String newPassword;
}
