package com.aradosavljevic.hr_service.domain.repository;



import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.enums.EmploymentType;
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
public interface WorkerRepository extends JpaRepository<Worker,Long> {

    Optional<Worker> findByEmail(String email);

    Optional<Worker> findByPersonalId(String personalId);

    List<Worker> findByEmploymentStatus(EmploymentStatus status);

    List<Worker> findByEmploymentType(EmploymentType type);

    @Query("SELECT w FROM Worker w WHERE w.employmentStatus = :status AND w.employmentType = :type")
    List<Worker> findByStatusAndType(@Param("status") EmploymentStatus status,
                                     @Param("type") EmploymentType type);

    @Query("SELECT w FROM Worker w WHERE " +
            "LOWER(w.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(w.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(w.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "w.personalId LIKE CONCAT('%', :searchTerm, '%')")
    Page<Worker> searchWorkers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT w FROM Worker w WHERE w.hireDate BETWEEN :startDate AND :endDate")
    List<Worker> findByHireDateBetween(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);


    @Query("SELECT COUNT(w) FROM Worker w WHERE w.employmentStatus = :status")
    long countByStatus(@Param("status") EmploymentStatus status);

    @NotNull
    Page<Worker> findAll(@NotNull Pageable pageable);
}
