package com.aradosavljevic.schedule_service.application.request.booking;

import com.aradosavljevic.schedule_service.domain.enums.TeachingType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingCreateRequest {

    @NotNull(message = "roomId je obavezan")
    private Long roomId;

    @NotNull(message = "requesterWorkerId je obavezan")
    private Long requesterWorkerId;

    private Long schoolYearId;

    @NotNull(message = "Pocetak termina (startDateTime) je obavezan")
    private LocalDateTime startDateTime;

    @NotNull(message = "Kraj termina (endDateTime) je obavezan")
    private LocalDateTime endDateTime;

    private String purpose;

    private TeachingType teachingType;
}
