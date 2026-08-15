package com.aradosavljevic.hr_service.application.request.assignment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WorkerPositionAssignRequest {

    @NotNull(message = "workerId je obavezan")
    private Long workerId;

    @NotNull(message = "positionId je obavezan")
    private Long positionId;

    @NotNull(message = "Datum pocetka (validFrom) je obavezan")
    private LocalDate validFrom;

    private LocalDate validTo;

    private BigDecimal fraction;

    private Boolean isPrimary;
}
