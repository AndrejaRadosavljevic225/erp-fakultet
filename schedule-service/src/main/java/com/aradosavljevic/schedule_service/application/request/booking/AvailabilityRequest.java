package com.aradosavljevic.schedule_service.application.request.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AvailabilityRequest {

    @NotNull(message = "roomId je obavezan")
    private Long roomId;

    @NotNull(message = "startDateTime je obavezan")
    private LocalDateTime startDateTime;

    @NotNull(message = "endDateTime je obavezan")
    private LocalDateTime endDateTime;
}
