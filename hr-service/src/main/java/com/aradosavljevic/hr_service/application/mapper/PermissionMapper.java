package com.aradosavljevic.hr_service.application.mapper;

import com.aradosavljevic.hr_service.application.dto.PermissionDTO;
import com.aradosavljevic.hr_service.domain.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionDTO toDTO(Permission p) {
        if (p == null) return null;
        return PermissionDTO.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .module(p.getModule())
                .build();
    }
}
