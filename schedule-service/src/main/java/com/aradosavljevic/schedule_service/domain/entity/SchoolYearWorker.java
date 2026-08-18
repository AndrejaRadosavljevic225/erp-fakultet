package com.aradosavljevic.schedule_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * school_year_worker iz modela — dodela profesora skolskoj godini,
 * povezana sa normom (norm_id) preko koje nasledi kvotu casova.
 */
@Entity
@Table(name = "school_year_worker", indexes = {
        @Index(name = "idx_syw_worker_year", columnList = "worker_id, school_year_id")
})
@Getter
@Setter
public class SchoolYearWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long schoolYearId;

    private Long workerId;

    private Long roleId;

    private Long normId;
}
