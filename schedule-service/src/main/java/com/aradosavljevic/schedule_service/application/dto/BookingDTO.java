package com.aradosavljevic.schedule_service.application.dto;

import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import com.aradosavljevic.schedule_service.domain.enums.TeachingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long roomId;
    private String roomName;
    private Long requesterWorkerId;
    private Long approvedBy;
    private Long schoolYearId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private double durationHours;
    private String purpose;
    private TeachingType teachingType;
    private BookingStatus status;
    private String notes;
}
