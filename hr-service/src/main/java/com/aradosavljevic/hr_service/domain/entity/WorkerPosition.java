package com.aradosavljevic.hr_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "worker_position", indexes = {
        @Index(name = "idx_wp_worker_id", columnList = "worker_id"),
        @Index(name = "idx_wp_position_id", columnList = "position_id")
})
@Getter
@Setter
public class WorkerPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Long workerId;

    private Long positionId;

    private LocalDate validFrom;

    private LocalDate validTo;

    private BigDecimal fraction;

    private Boolean isPrimary = false;

    // Helper methods
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !validFrom.isAfter(now) && (validTo == null || !validTo.isBefore(now));
    }

    public boolean isActiveOn(LocalDate date) {
        return !validFrom.isAfter(date) && (validTo == null || !validTo.isBefore(date));
    }
}
