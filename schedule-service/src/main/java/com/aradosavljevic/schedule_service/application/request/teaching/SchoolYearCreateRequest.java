package com.aradosavljevic.schedule_service.application.request.teaching;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SchoolYearCreateRequest {

    @NotBlank(message = "Kod skolske godine je obavezan")
    private String code;

    @NotNull(message = "Datum pocetka je obavezan")
    private LocalDate startDate;

    @NotNull(message = "Datum kraja je obavezan")
    private LocalDate endDate;

    private String description;
}
