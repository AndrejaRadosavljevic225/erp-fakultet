package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.RoleDTO;
import com.aradosavljevic.hr_service.application.request.role.RoleCreateRequest;
import com.aradosavljevic.hr_service.application.request.role.RoleUpdateRequest;
import com.aradosavljevic.hr_service.application.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<PageResponse<RoleDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(roleService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getById(id));
    }

    @PostMapping
    public ApiResponse<RoleDTO> create(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.success("Rola je kreirana", roleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleDTO> update(@PathVariable Long id,
                                       @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.success("Rola je azurirana", roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success("Rola je obrisana", null);
    }
}
