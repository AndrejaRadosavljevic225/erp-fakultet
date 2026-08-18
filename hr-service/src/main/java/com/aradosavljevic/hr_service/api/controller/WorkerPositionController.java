package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.hr_service.application.dto.WorkerPositionDTO;
import com.aradosavljevic.hr_service.application.request.assignment.WorkerPositionAssignRequest;
import com.aradosavljevic.hr_service.application.request.assignment.WorkerPositionUpdateRequest;
import com.aradosavljevic.hr_service.application.service.WorkerPositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker-positions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HR')")
public class WorkerPositionController {

    private final WorkerPositionService workerPositionService;

    @GetMapping("/worker/{workerId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PROFESOR')")
    public ApiResponse<List<WorkerPositionDTO>> getByWorker(@PathVariable Long workerId) {
        return ApiResponse.success(workerPositionService.getByWorker(workerId));
    }

    @PostMapping
    public ApiResponse<WorkerPositionDTO> assign(@Valid @RequestBody WorkerPositionAssignRequest request) {
        return ApiResponse.success("Pozicija je dodeljena radniku", workerPositionService.assign(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkerPositionDTO> update(@PathVariable Long id,
                                                 @Valid @RequestBody WorkerPositionUpdateRequest request) {
        return ApiResponse.success("Dodela je azurirana", workerPositionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        workerPositionService.remove(id);
        return ApiResponse.success("Dodela je uklonjena", null);
    }
}
