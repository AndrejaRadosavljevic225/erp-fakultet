package com.aradosavljevic.hr_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityName;

    private Long entityId;

    private String action;

    private Long changedBy;

    @Column(length = 1000)
    private String details;

    @CreationTimestamp
    private LocalDateTime changedAt;
}
