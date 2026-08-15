package com.aradosavljevic.hr_service.application.request.worker;

import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.enums.EmploymentType;
import lombok.Data;

@Data
public class WorkerSearchRequest {
    private String searchTerm;
    private EmploymentStatus employmentStatus;
    private EmploymentType employmentType;
}
