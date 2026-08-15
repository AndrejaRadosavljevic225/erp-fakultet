package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.AuditLogDTO;
import com.aradosavljevic.hr_service.application.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ApiResponse<PageResponse<AuditLogDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(auditService.getAll(pageable));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    public ApiResponse<List<AuditLogDTO>> getForEntity(@PathVariable String entityName,
                                                       @PathVariable Long entityId) {
        return ApiResponse.success(auditService.getForEntity(entityName, entityId));
    }
}
