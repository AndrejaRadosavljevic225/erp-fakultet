package com.aradosavljevic.schedule_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * teaching_norm iz modela — KVOTA casova po zvanju (roli) za skolsku godinu.
 */
@Entity
@Getter
@Setter
public class TeachingNorm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roleId;

    private Long schoolYearId;

    private Integer requiredHours;

    private String description;
}
