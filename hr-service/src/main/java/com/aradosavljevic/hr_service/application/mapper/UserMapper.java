package com.aradosavljevic.hr_service.application.mapper;

import com.aradosavljevic.hr_service.application.dto.UserDTO;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(UserAccount u, String roleName) {
        if (u == null) return null;
        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .workerId(u.getWorkerId())
                .roleId(u.getRoleId())
                .roleName(roleName)
                .active(Boolean.TRUE.equals(u.getIsActive()))
                .createdAt(u.getCreatedAt())
                .lastLogin(u.getLastLogin())
                .build();
    }
}
