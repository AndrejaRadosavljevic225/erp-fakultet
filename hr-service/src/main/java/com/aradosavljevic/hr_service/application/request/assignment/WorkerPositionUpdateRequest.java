package com.aradosavljevic.hr_service.application.request.assignment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WorkerPositionUpdateRequest {
    private LocalDate validFrom;
    private LocalDate validTo;
    private BigDecimal fraction;
    private Boolean isPrimary;
}
