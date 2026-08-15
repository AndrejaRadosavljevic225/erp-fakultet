package com.aradosavljevic.hr_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerPositionDTO {
    private Long id;
    private Long workerId;
    private Long positionId;
    private String positionTitle;
    private LocalDate validFrom;
    private LocalDate validTo;
    private BigDecimal fraction;
    private Boolean isPrimary;
    private boolean active;
}
