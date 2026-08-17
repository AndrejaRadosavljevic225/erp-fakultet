package com.aradosavljevic.schedule_service.application.mapper;

import com.aradosavljevic.schedule_service.application.dto.SchoolYearDTO;
import com.aradosavljevic.schedule_service.domain.entity.SchoolYear;
import org.springframework.stereotype.Component;

@Component
public class SchoolYearMapper {

    public SchoolYearDTO toDTO(SchoolYear sy) {
        if (sy == null) return null;
        return SchoolYearDTO.builder()
                .id(sy.getId())
                .code(sy.getCode())
                .startDate(sy.getStartDate())
                .endDate(sy.getEndDate())
                .description(sy.getDescription())
                .build();
    }
}
