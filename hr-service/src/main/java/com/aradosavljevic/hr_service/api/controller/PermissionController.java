package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.PermissionDTO;
import com.aradosavljevic.hr_service.application.request.permission.PermissionCreateRequest;
import com.aradosavljevic.hr_service.application.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<PageResponse<PermissionDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(permissionService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<PermissionDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(permissionService.getById(id));
    }

    @PostMapping
    public ApiResponse<PermissionDTO> create(@Valid @RequestBody PermissionCreateRequest request) {
        return ApiResponse.success("Permisija je kreirana", permissionService.create(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.success("Permisija je obrisana", null);
    }
}
