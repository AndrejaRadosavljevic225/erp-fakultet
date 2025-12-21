package com.aradosavljevic.hr_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String salaryGrade;

    private BigDecimal baseSalary;

    private Boolean isVacant = true;

    @CreationTimestamp
    private LocalDateTime createdAt;


    // Helper methods
    public void markAsOccupied() {
        this.isVacant = false;
    }

    public void markAsVacant() {
        this.isVacant = true;
    }
}
