package com.aradosavljevic.schedule_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingNormDTO {
    private Long id;
    private Long roleId;
    private Long schoolYearId;
    private Integer requiredHours;
    private String description;
}
