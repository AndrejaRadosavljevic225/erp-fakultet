package com.aradosavljevic.hr_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Podaci o trenutno prijavljenom korisniku (GET /api/auth/me).
 * Frontend odavde uzima rolu, workerId i permisije umesto da dekodira JWT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {

    private Long userId;
    private String username;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Long workerId;
    private String workerFullName;
    private String workerEmail;
    private List<String> permissions;
}
