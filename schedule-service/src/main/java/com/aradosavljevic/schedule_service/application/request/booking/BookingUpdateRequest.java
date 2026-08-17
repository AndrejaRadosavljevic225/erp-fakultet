package com.aradosavljevic.schedule_service.application.request.booking;

import com.aradosavljevic.schedule_service.domain.enums.TeachingType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingUpdateRequest {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String purpose;
    private TeachingType teachingType;
    private String notes;
}
