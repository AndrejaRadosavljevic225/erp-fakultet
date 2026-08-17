package com.aradosavljevic.schedule_service.application.request.teaching;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TeachingNormCreateRequest {

    @NotNull(message = "roleId je obavezan")
    private Long roleId;

    @NotNull(message = "schoolYearId je obavezan")
    private Long schoolYearId;

    @NotNull(message = "requiredHours (kvota) je obavezan")
    @PositiveOrZero(message = "requiredHours mora biti >= 0")
    private Integer requiredHours;

    private String description;
}
