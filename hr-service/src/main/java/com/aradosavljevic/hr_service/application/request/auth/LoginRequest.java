package com.aradosavljevic.hr_service.application.request.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /**
     * Prijava je moguca korisnickim imenom ILI email-om zaposlenog (UC-G-01).
     * Stari kljuc "username" ostaje podrzan preko @JsonAlias.
     */
    @NotBlank(message = "Korisnicko ime ili email je obavezan")
    @JsonAlias({"username", "email"})
    private String usernameOrEmail;

    @NotBlank(message = "Lozinka je obavezna")
    private String password;
}
