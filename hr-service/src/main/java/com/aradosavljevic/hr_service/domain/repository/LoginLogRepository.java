package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.LoginLog;
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
public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {

    List<LoginLog> findByUserUserId(UUID userId);

    Page<LoginLog> findByUserUserIdOrderByLoginTimeDesc(UUID userId, Pageable pageable);

    @Query("SELECT ll FROM LoginLog ll WHERE ll.user.userId = :userId AND ll.logoutTime IS NULL")
    Optional<LoginLog> findActiveSessionByUserId(@Param("userId") UUID userId);

    @Query("SELECT ll FROM LoginLog ll WHERE ll.loginTime BETWEEN :startTime AND :endTime")
    List<LoginLog> findByLoginTimeBetween(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(ll) FROM LoginLog ll WHERE ll.user.userId = :userId AND ll.status = 'LOGIN'")
    long countSuccessfulLoginsByUserId(@Param("userId") UUID userId);
}
