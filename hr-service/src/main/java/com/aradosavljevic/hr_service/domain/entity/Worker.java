package com.aradosavljevic.hr_service.domain.entity;


import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "worker", indexes = {
        @Index(name = "idx_worker_email", columnList = "email", unique = true),
        @Index(name = "idx_worker_personal_id", columnList = "personal_id", unique = true),
        @Index(name = "idx_worker_status", columnList = "employment_status")
})
@Getter
@Setter
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String personalId;

    private String phone;

    private LocalDate hireDate;

    private LocalDate terminationDate = null;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return employmentStatus == EmploymentStatus.ACTIVE;
    }

}
