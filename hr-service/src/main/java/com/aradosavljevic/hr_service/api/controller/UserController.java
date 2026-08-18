package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.UserDTO;
import com.aradosavljevic.hr_service.application.request.user.UserCreateRequest;
import com.aradosavljevic.hr_service.application.request.user.UserUpdateRequest;
import com.aradosavljevic.hr_service.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HR')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(userService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    @PostMapping
    public ApiResponse<UserDTO> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success("Korisnik je kreiran", userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDTO> update(@PathVariable Long id,
                                       @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success("Korisnik je azuriran", userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("Korisnik je obrisan", null);
    }
}
