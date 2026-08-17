package com.aradosavljevic.schedule_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearDTO {
    private Long id;
    private String code;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
