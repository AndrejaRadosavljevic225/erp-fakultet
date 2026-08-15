package com.aradosavljevic.hr_service.application.request.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionCreateRequest {

    @NotBlank(message = "Kod permisije je obavezan")
    private String code;

    @NotBlank(message = "Naziv permisije je obavezan")
    private String name;

    private String module;
}
