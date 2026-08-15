package com.aradosavljevic.hr_service.application.mapper;

import com.aradosavljevic.hr_service.application.dto.PositionDTO;
import com.aradosavljevic.hr_service.domain.entity.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public PositionDTO toDTO(Position p) {
        if (p == null) return null;
        return PositionDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .salaryGrade(p.getSalaryGrade())
                .baseSalary(p.getBaseSalary())
                .vacant(Boolean.TRUE.equals(p.getIsVacant()))
                .build();
    }
}
