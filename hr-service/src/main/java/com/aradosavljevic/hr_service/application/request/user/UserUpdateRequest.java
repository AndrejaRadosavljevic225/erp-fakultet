package com.aradosavljevic.hr_service.application.request.user;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long workerId;
    private Long roleId;
    private Boolean isActive;
}
