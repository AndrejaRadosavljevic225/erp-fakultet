package com.aradosavljevic.hr_service.application.request.position;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionCreateRequest {

    @NotBlank(message = "Naziv pozicije je obavezan")
    private String title;

    private String salaryGrade;

    private BigDecimal baseSalary;

    private Boolean isVacant;
}
