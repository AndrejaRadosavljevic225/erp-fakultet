package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.hr_service.application.dto.PermissionDTO;
import com.aradosavljevic.hr_service.application.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/api/roles/{roleId}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<List<PermissionDTO>> getRolePermissions(@PathVariable Long roleId) {
        return ApiResponse.success(rolePermissionService.getPermissionsOfRole(roleId));
    }

    @PostMapping("/api/roles/{roleId}/permissions/{permissionId}")
    public ApiResponse<PermissionDTO> assign(@PathVariable Long roleId,
                                             @PathVariable Long permissionId) {
        return ApiResponse.success("Permisija je dodeljena roli",
                rolePermissionService.assignPermissionToRole(roleId, permissionId));
    }

    @DeleteMapping("/api/roles/{roleId}/permissions/{permissionId}")
    public ApiResponse<Void> remove(@PathVariable Long roleId,
                                    @PathVariable Long permissionId) {
        rolePermissionService.removePermissionFromRole(roleId, permissionId);
        return ApiResponse.success("Permisija je uklonjena sa role", null);
    }

    @GetMapping("/api/users/{userId}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<List<PermissionDTO>> getUserPermissions(@PathVariable Long userId) {
        return ApiResponse.success(rolePermissionService.getPermissionsOfUser(userId));
    }
}
