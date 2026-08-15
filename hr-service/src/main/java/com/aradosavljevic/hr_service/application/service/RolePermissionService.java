package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.hr_service.application.dto.PermissionDTO;

import java.util.List;

public interface RolePermissionService {

    List<PermissionDTO> getPermissionsOfRole(Long roleId);

    List<PermissionDTO> getPermissionsOfUser(Long userId);

    PermissionDTO assignPermissionToRole(Long roleId, Long permissionId);

    void removePermissionFromRole(Long roleId, Long permissionId);
}
