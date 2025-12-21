package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    Optional<Role> findByRoleName(String roleName);

    List<Role> findByIsActive(Boolean isActive);

    boolean existsByCode(String code);

    @Override
    @NotNull
    Page<Role> findAll(@NotNull Pageable pageable);

//    TODO: Videti da izmenim upit
//    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permissionId " +
//            "WHERE rp.roleId = :roleId")
//    Optional<Role> findByIdWithPermissions(@Param("roleId") Long roleId);
}
