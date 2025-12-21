package com.aradosavljevic.hr_service.domain.repository;


import com.aradosavljevic.hr_service.domain.entity.Permission;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByModule(String module);

    boolean existsByCode(String code);

    @NotNull
    Page<Permission> findAll(@NotNull Pageable pageable);

}
