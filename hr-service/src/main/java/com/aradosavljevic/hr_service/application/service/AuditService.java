package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.AuditLogDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {

    void log(String entityName, Long entityId, String action, String details);

    PageResponse<AuditLogDTO> getAll(Pageable pageable);

    List<AuditLogDTO> getForEntity(String entityName, Long entityId);
}
