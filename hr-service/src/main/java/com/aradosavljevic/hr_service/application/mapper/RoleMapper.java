package com.aradosavljevic.hr_service.application.mapper;

import com.aradosavljevic.hr_service.application.dto.RoleDTO;
import com.aradosavljevic.hr_service.domain.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleDTO toDTO(Role r) {
        if (r == null) return null;
        return RoleDTO.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .active(Boolean.TRUE.equals(r.getIsActive()))
                .build();
    }
}
