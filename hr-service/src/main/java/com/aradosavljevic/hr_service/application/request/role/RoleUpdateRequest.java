package com.aradosavljevic.hr_service.application.request.role;

import lombok.Data;

@Data
public class RoleUpdateRequest {
    private String name;
    private String description;
    private Boolean isActive;
}
