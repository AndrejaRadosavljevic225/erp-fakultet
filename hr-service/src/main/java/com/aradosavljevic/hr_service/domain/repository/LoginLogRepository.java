package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.LoginLog;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByUserUserId(Long userId);

    Page<LoginLog> findByUserUserIdOrderByLoginTimeDesc(UUID userId, Pageable pageable);

    @Query("SELECT ll FROM LoginLog ll WHERE ll.userId = :userId AND ll.logoutTime IS NULL")
    Optional<LoginLog> findActiveSessionByUserId(@Param("userId") Long userId);

    @Query("SELECT ll FROM LoginLog ll WHERE ll.loginTime BETWEEN :startTime AND :endTime")
    List<LoginLog> findByLoginTimeBetween(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(ll) FROM LoginLog ll WHERE ll.userId = :userId AND ll.status = 'LOGIN'")
    long countSuccessfulLoginsByUserId(@Param("userId") UUID userId);


    @NotNull
    Page<LoginLog> findAll(@NotNull Pageable pageable);
}
