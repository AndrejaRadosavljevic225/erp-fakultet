package com.aradosavljevic.hr_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "role_permission", indexes = {
        @Index(name = "idx_rp_role_id", columnList = "role_id"),
        @Index(name = "idx_rp_permission_id", columnList = "permission_id")
})
@Getter
@Setter
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roleId;

    private Long permissionId;
}

