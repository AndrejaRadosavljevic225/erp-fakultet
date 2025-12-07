package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleRoleId(UUID roleId);

    List<RolePermission> findByPermissionPermissionId(UUID permissionId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleId = :roleId AND rp.permission.permissionId = :permissionId")
    boolean existsByRoleAndPermission(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);
}
