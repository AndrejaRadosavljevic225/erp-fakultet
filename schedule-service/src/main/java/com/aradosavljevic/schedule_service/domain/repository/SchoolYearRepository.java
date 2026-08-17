package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.SchoolYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYear, Long> {

    Optional<SchoolYear> findByCode(String code);

    boolean existsByCode(String code);

    Page<SchoolYear> findAll(Pageable pageable);
}
