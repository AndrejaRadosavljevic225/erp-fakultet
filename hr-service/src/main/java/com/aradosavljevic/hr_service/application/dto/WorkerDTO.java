package com.aradosavljevic.hr_service.application.dto;

import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.enums.EmploymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String personalId;
    private String phone;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private EmploymentStatus employmentStatus;
    private EmploymentType employmentType;
    private boolean active;
}
