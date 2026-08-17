package com.aradosavljevic.schedule_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.SchoolYearDTO;
import com.aradosavljevic.schedule_service.application.request.teaching.SchoolYearCreateRequest;
import com.aradosavljevic.schedule_service.application.service.SchoolYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school-years")
@RequiredArgsConstructor
public class SchoolYearController {

    private final SchoolYearService schoolYearService;

    @GetMapping
    public ApiResponse<PageResponse<SchoolYearDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(schoolYearService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<SchoolYearDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(schoolYearService.getById(id));
    }

    @PostMapping
    public ApiResponse<SchoolYearDTO> create(@Valid @RequestBody SchoolYearCreateRequest request) {
        return ApiResponse.success("Skolska godina je kreirana", schoolYearService.create(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        schoolYearService.delete(id);
        return ApiResponse.success("Skolska godina je obrisana", null);
    }
}
