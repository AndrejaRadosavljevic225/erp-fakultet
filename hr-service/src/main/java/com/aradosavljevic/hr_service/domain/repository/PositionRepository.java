package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.Position;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {

    List<Position> findByIsVacant(Boolean isVacant);

    List<Position> findBySalaryGrade(String salaryGrade);

    //TODO: ovo moze biti korisno za druge implementaciej
    @Query("SELECT p FROM Position p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Position> findByTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Position p WHERE p.isVacant = true")
    long countVacantPositions();

    @Query("SELECT COUNT(p) FROM Position p WHERE p.isVacant = false")
    long countOccupiedPositions();

    @NotNull
    Page<Position> findAll(@NotNull Pageable pageable);
}
