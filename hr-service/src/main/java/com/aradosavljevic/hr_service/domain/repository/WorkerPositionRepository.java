package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.entity.WorkerPosition;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerPositionRepository extends JpaRepository<WorkerPosition, Long> {

    List<WorkerPosition> findByWorkerId(Long workerId);

    List<WorkerPosition> findByPositionId(Long positionId);

    @Query("SELECT wp FROM WorkerPosition wp WHERE wp.workerId = :workerId AND wp.isPrimary = true")
    Optional<WorkerPosition> findPrimaryPositionByWorkerId(@Param("workerId") Long workerId);

    @Query("SELECT wp FROM WorkerPosition wp WHERE " +
            "wp.workerId = :workerId AND " +
            "wp.validFrom <= :date AND " +
            "(wp.validTo IS NULL OR wp.validTo >= :date)")
    List<WorkerPosition> findActivePositionsByWorkerIdOnDate(@Param("workerId") Long workerId,
                                                             @Param("date") LocalDate date);

    @Query("SELECT wp FROM WorkerPosition wp WHERE " +
            "wp.positionId = :positionId AND " +
            "wp.validFrom <= CURRENT_DATE AND " +
            "(wp.validTo IS NULL OR wp.validTo >= CURRENT_DATE)")
    List<WorkerPosition> findCurrentWorkersByPositionId(@Param("positionId") Long positionId);

    @Query("SELECT CASE WHEN COUNT(wp) > 0 THEN true ELSE false END FROM WorkerPosition wp WHERE " +
            "wp.positionId = :positionId AND " +
            "wp.validFrom <= :endDate AND " +
            "(wp.validTo IS NULL OR wp.validTo >= :startDate)")
    boolean isPositionOccupiedInPeriod(@Param("positionId") Long positionId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
    @NotNull
    Page<WorkerPosition> findAll(@NotNull Pageable pageable);
}
