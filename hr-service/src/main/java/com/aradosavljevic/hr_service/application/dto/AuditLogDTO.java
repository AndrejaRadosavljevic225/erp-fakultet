package com.aradosavljevic.hr_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private Long changedBy;
    private String details;
    private LocalDateTime changedAt;
}
