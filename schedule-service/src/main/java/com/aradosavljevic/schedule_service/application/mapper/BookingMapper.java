package com.aradosavljevic.schedule_service.application.mapper;

import com.aradosavljevic.schedule_service.application.dto.BookingDTO;
import com.aradosavljevic.schedule_service.domain.entity.Booking;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class BookingMapper {

    public BookingDTO toDTO(Booking b, String roomName) {
        if (b == null) return null;
        return BookingDTO.builder()
                .id(b.getId())
                .roomId(b.getRoomId())
                .roomName(roomName)
                .requesterWorkerId(b.getRequesterWorkerId())
                .approvedBy(b.getApprovedBy())
                .schoolYearId(b.getSchoolYearId())
                .startDateTime(b.getStartDateTime())
                .endDateTime(b.getEndDateTime())
                .durationHours(durationHours(b))
                .purpose(b.getPurpose())
                .teachingType(b.getTeachingType())
                .status(b.getStatus())
                .notes(b.getNotes())
                .build();
    }

    public static double durationHours(Booking b) {
        if (b.getStartDateTime() == null || b.getEndDateTime() == null) return 0;
        long minutes = Duration.between(b.getStartDateTime(), b.getEndDateTime()).toMinutes();
        return Math.round(minutes / 60.0 * 100.0) / 100.0;
    }
}
