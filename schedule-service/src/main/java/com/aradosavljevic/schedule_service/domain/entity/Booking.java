package com.aradosavljevic.schedule_service.domain.entity;

import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import com.aradosavljevic.schedule_service.domain.enums.TeachingType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * room_scheduled iz modela — rezervacija/termin u prostoriji.
 */
@Entity
@Table(name = "booking", indexes = {
        @Index(name = "idx_booking_room_time", columnList = "room_id, start_date_time, end_date_time"),
        @Index(name = "idx_booking_worker", columnList = "requester_worker_id"),
        @Index(name = "idx_booking_year", columnList = "school_year_id")
})
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    private Long requesterWorkerId;

    private Long approvedBy;

    private Long schoolYearId;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String purpose;

    @Enumerated(EnumType.STRING)
    private TeachingType teachingType;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.REQUESTED;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
