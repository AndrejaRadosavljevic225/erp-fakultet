package com.aradosavljevic.hr_service.application.request.worker;

import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.enums.EmploymentType;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkerUpdateRequest {

    private String firstName;
    private String lastName;

    @Email(message = "Email nije validan")
    private String email;

    private String personalId;
    private String phone;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private EmploymentStatus employmentStatus;
    private EmploymentType employmentType;
}
