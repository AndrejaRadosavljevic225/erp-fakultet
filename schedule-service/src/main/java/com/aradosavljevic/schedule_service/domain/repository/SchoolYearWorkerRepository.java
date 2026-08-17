package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.SchoolYearWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolYearWorkerRepository extends JpaRepository<SchoolYearWorker, Long> {

    List<SchoolYearWorker> findBySchoolYearId(Long schoolYearId);

    List<SchoolYearWorker> findByWorkerId(Long workerId);

    Optional<SchoolYearWorker> findByWorkerIdAndSchoolYearId(Long workerId, Long schoolYearId);

    boolean existsByWorkerIdAndSchoolYearId(Long workerId, Long schoolYearId);
}
