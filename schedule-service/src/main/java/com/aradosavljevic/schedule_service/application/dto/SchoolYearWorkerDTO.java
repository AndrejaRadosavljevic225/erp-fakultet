package com.aradosavljevic.schedule_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearWorkerDTO {
    private Long id;
    private Long schoolYearId;
    private Long workerId;
    private Long roleId;
    private Long normId;
}
