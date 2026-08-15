package com.aradosavljevic.hr_service.application.request.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleCreateRequest {

    @NotBlank(message = "Kod role je obavezan")
    private String code;

    @NotBlank(message = "Naziv role je obavezan")
    private String name;

    private String description;
}
