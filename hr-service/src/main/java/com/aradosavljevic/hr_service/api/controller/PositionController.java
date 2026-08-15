package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.PositionDTO;
import com.aradosavljevic.hr_service.application.request.position.PositionCreateRequest;
import com.aradosavljevic.hr_service.application.request.position.PositionUpdateRequest;
import com.aradosavljevic.hr_service.application.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public ApiResponse<PageResponse<PositionDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(positionService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<PositionDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(positionService.getById(id));
    }

    @PostMapping
    public ApiResponse<PositionDTO> create(@Valid @RequestBody PositionCreateRequest request) {
        return ApiResponse.success("Pozicija je kreirana", positionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PositionDTO> update(@PathVariable Long id,
                                           @Valid @RequestBody PositionUpdateRequest request) {
        return ApiResponse.success("Pozicija je azurirana", positionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ApiResponse.success("Pozicija je obrisana", null);
    }
}
