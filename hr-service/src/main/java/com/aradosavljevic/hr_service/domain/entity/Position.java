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
@Table(name = "position", indexes = {
        @Index(name = "idx_position_title", columnList = "title"),
        @Index(name = "idx_position_vacant", columnList = "is_vacant")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "position_id")
    private UUID positionId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "salary_grade", length = 50)
    private String salaryGrade;

    @Column(name = "base_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "is_vacant", nullable = false)
    @Builder.Default
    private Boolean isVacant = true;

    @Column(name = "budget_line", length = 100)
    private String budgetLine;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkerPosition> workerPositions = new ArrayList<>();

    // Helper methods
    public void markAsOccupied() {
        this.isVacant = false;
    }

    public void markAsVacant() {
        this.isVacant = true;
    }
}
