package com.aradosavljevic.hr_service.application.dto;

import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private EmploymentStatus employmentStatus;
}
