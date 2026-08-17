package com.aradosavljevic.schedule_service.application.request.teaching;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchoolYearWorkerRequest {

    @NotNull(message = "schoolYearId je obavezan")
    private Long schoolYearId;

    @NotNull(message = "workerId je obavezan")
    private Long workerId;

    private Long roleId;

    // Opciono: eksplicitna norma. Ako je null, pokusace se naci norma po roli+godini.
    private Long normId;
}
