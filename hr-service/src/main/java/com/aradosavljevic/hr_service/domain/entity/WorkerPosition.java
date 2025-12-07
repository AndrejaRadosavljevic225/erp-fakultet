package com.aradosavljevic.hr_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "worker_position", indexes = {
        @Index(name = "idx_worker_position_worker", columnList = "worker_id"),
        @Index(name = "idx_worker_position_position", columnList = "position_id"),
        @Index(name = "idx_worker_position_dates", columnList = "valid_from, valid_to"),
        @Index(name = "idx_worker_position_primary", columnList = "is_primary")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id")
    private UUID assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "fraction", nullable = false, precision = 3, scale = 2)
    private BigDecimal fraction;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
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
