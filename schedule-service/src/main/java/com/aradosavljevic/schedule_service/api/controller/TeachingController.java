package com.aradosavljevic.schedule_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.SchoolYearWorkerDTO;
import com.aradosavljevic.schedule_service.application.dto.TeachingNormDTO;
import com.aradosavljevic.schedule_service.application.dto.TeachingReportDTO;
import com.aradosavljevic.schedule_service.application.request.teaching.SchoolYearWorkerRequest;
import com.aradosavljevic.schedule_service.application.request.teaching.TeachingNormCreateRequest;
import com.aradosavljevic.schedule_service.application.service.TeachingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teaching")
@RequiredArgsConstructor
public class TeachingController {

    private final TeachingService teachingService;

    // --- Norme (kvote) ---

    @GetMapping("/norms")
    public ApiResponse<PageResponse<TeachingNormDTO>> getNorms(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(teachingService.getAllNorms(pageable));
    }

    @PostMapping("/norms")
    public ApiResponse<TeachingNormDTO> createNorm(@Valid @RequestBody TeachingNormCreateRequest request) {
        return ApiResponse.success("Norma je kreirana", teachingService.createNorm(request));
    }

    @DeleteMapping("/norms/{id}")
    public ApiResponse<Void> deleteNorm(@PathVariable Long id) {
        teachingService.deleteNorm(id);
        return ApiResponse.success("Norma je obrisana", null);
    }

    // --- Dodela profesora godini ---

    @GetMapping("/assignments/year/{schoolYearId}")
    public ApiResponse<List<SchoolYearWorkerDTO>> getAssignments(@PathVariable Long schoolYearId) {
        return ApiResponse.success(teachingService.getAssignmentsByYear(schoolYearId));
    }

    @PostMapping("/assignments")
    public ApiResponse<SchoolYearWorkerDTO> assign(@Valid @RequestBody SchoolYearWorkerRequest request) {
        return ApiResponse.success("Profesor je dodeljen skolskoj godini", teachingService.assignWorker(request));
    }

    @DeleteMapping("/assignments/{id}")
    public ApiResponse<Void> removeAssignment(@PathVariable Long id) {
        teachingService.removeAssignment(id);
        return ApiResponse.success("Dodela je uklonjena", null);
    }

    // --- Fond casova (izvestaj) ---

    @GetMapping("/report")
    public ApiResponse<TeachingReportDTO> report(@RequestParam Long workerId,
                                                 @RequestParam Long schoolYearId) {
        return ApiResponse.success(teachingService.report(workerId, schoolYearId));
    }

    @GetMapping("/report/year/{schoolYearId}")
    public ApiResponse<List<TeachingReportDTO>> reportByYear(@PathVariable Long schoolYearId) {
        return ApiResponse.success(teachingService.reportByYear(schoolYearId));
    }
}
