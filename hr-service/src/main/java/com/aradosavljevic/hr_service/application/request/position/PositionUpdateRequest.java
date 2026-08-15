package com.aradosavljevic.hr_service.application.request.position;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionUpdateRequest {
    private String title;
    private String salaryGrade;
    private BigDecimal baseSalary;
    private Boolean isVacant;
}
