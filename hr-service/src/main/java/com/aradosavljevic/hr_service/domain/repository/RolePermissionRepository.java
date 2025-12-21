package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.RolePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByPermissionId(Long permissionId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId = :permissionId")
    boolean existsByRoleAndPermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    Page<RolePermission> findByRoleId(Long roleId, Pageable pageable);

    Page<RolePermission> findAll(Pageable pageable);
}
