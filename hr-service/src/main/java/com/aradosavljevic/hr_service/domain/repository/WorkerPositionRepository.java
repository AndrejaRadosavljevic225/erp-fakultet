package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.WorkerPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerPositionRepository extends JpaRepository<WorkerPosition, UUID> {

    List<WorkerPosition> findByWorkerWorkerId(UUID workerId);

    List<WorkerPosition> findByPositionPositionId(UUID positionId);

    @Query("SELECT wp FROM WorkerPosition wp WHERE wp.worker.workerId = :workerId AND wp.isPrimary = true")
    Optional<WorkerPosition> findPrimaryPositionByWorkerId(@Param("workerId") UUID workerId);

    @Query("SELECT wp FROM WorkerPosition wp WHERE " +
            "wp.worker.workerId = :workerId AND " +
            "wp.validFrom <= :date AND " +
            "(wp.validTo IS NULL OR wp.validTo >= :date)")
    List<WorkerPosition> findActivePositionsByWorkerIdOnDate(@Param("workerId") UUID workerId,
                                                             @Param("date") LocalDate date);

    @Query("SELECT wp FROM WorkerPosition wp WHERE " +
            "wp.position.positionId = :positionId AND " +
            "wp.validFrom <= CURRENT_DATE AND " +
            "(wp.validTo IS NULL OR wp.validTo >= CURRENT_DATE)")
    List<WorkerPosition> findCurrentWorkersByPositionId(@Param("positionId") UUID positionId);

    @Query("SELECT CASE WHEN COUNT(wp) > 0 THEN true ELSE false END FROM WorkerPosition wp WHERE " +
            "wp.position.positionId = :positionId AND " +
            "wp.validFrom <= :endDate AND " +
            "(wp.validTo IS NULL OR wp.validTo >= :startDate)")
    boolean isPositionOccupiedInPeriod(@Param("positionId") UUID positionId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}
