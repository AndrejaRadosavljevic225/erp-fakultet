package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.SchoolYear;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYear, Long> {

    Page<SchoolYear> findSchoolYearById(Long id, Pageable pageable);
    Page<SchoolYear> findSchoolYearByStartDateAndEndDate(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<SchoolYear> findAll(Pageable pageable);
}
