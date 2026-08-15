package com.aradosavljevic.hr_service.application.request.worker;

import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.enums.EmploymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkerCreateRequest {

    @NotBlank(message = "Ime je obavezno")
    private String firstName;

    @NotBlank(message = "Prezime je obavezno")
    private String lastName;

    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email nije validan")
    private String email;

    @NotBlank(message = "JMBG je obavezan")
    private String personalId;

    private String phone;

    @NotNull(message = "Datum zaposlenja je obavezan")
    private LocalDate hireDate;

    private EmploymentStatus employmentStatus;

    private EmploymentType employmentType;
}
