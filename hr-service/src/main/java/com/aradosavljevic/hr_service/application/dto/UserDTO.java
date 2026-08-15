package com.aradosavljevic.hr_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private Long workerId;
    private Long roleId;
    private String roleName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
