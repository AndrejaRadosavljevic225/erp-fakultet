package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.TeachingNorm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeachingNormRepository extends JpaRepository<TeachingNorm, Long> {

    Optional<TeachingNorm> findByRoleIdAndSchoolYearId(Long roleId, Long schoolYearId);

    List<TeachingNorm> findBySchoolYearId(Long schoolYearId);

    Page<TeachingNorm> findAll(Pageable pageable);
}
