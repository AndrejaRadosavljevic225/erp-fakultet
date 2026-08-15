package com.aradosavljevic.hr_service.application.mapper;

import com.aradosavljevic.hr_service.application.dto.AuditLogDTO;
import com.aradosavljevic.hr_service.domain.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditLogDTO toDTO(AuditLog a) {
        if (a == null) return null;
        return AuditLogDTO.builder()
                .id(a.getId())
                .entityName(a.getEntityName())
                .entityId(a.getEntityId())
                .action(a.getAction())
                .changedBy(a.getChangedBy())
                .details(a.getDetails())
                .changedAt(a.getChangedAt())
                .build();
    }
}
