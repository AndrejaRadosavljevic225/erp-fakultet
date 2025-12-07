package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByWorkerWorkerId(UUID workerId);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM UserAccount u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.rolePermissions rp " +
            "LEFT JOIN FETCH rp.permission WHERE u.username = :username")
    Optional<UserAccount> findByUsernameWithRoleAndPermissions(@Param("username") String username);

    @Query("SELECT COUNT(u) FROM UserAccount u WHERE u.isActive = true")
    long countActiveUsers();
}

