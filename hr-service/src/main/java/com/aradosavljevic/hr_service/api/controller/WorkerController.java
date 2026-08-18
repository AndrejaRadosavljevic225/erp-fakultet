package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.WorkerDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerDetailDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerSummaryDTO;
import com.aradosavljevic.hr_service.application.request.worker.WorkerCreateRequest;
import com.aradosavljevic.hr_service.application.request.worker.WorkerUpdateRequest;
import com.aradosavljevic.hr_service.application.service.WorkerService;
import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HR')")
public class WorkerController {

    private static final String READ = "hasAnyRole('ADMIN','HR','PROFESOR')";

    private final WorkerService workerService;

    @GetMapping
    @PreAuthorize(READ)
    public ApiResponse<PageResponse<WorkerSummaryDTO>> search(
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(workerService.search(searchTerm, pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(READ)
    public ApiResponse<List<WorkerSummaryDTO>> byStatus(@PathVariable EmploymentStatus status) {
        return ApiResponse.success(workerService.getByStatus(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize(READ)
    public ApiResponse<WorkerDetailDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(workerService.getById(id));
    }

    @PostMapping
    public ApiResponse<WorkerDTO> create(@Valid @RequestBody WorkerCreateRequest request) {
        return ApiResponse.success("Radnik je kreiran", workerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkerDTO> update(@PathVariable Long id,
                                         @Valid @RequestBody WorkerUpdateRequest request) {
        return ApiResponse.success("Radnik je azuriran", workerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workerService.delete(id);
        return ApiResponse.success("Radnik je obrisan", null);
    }
}
