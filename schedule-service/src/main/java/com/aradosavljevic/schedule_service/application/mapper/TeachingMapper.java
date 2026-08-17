package com.aradosavljevic.schedule_service.application.mapper;

import com.aradosavljevic.schedule_service.application.dto.SchoolYearWorkerDTO;
import com.aradosavljevic.schedule_service.application.dto.TeachingNormDTO;
import com.aradosavljevic.schedule_service.domain.entity.SchoolYearWorker;
import com.aradosavljevic.schedule_service.domain.entity.TeachingNorm;
import org.springframework.stereotype.Component;

@Component
public class TeachingMapper {

    public TeachingNormDTO toNormDTO(TeachingNorm n) {
        if (n == null) return null;
        return TeachingNormDTO.builder()
                .id(n.getId())
                .roleId(n.getRoleId())
                .schoolYearId(n.getSchoolYearId())
                .requiredHours(n.getRequiredHours())
                .description(n.getDescription())
                .build();
    }

    public SchoolYearWorkerDTO toWorkerDTO(SchoolYearWorker w) {
        if (w == null) return null;
        return SchoolYearWorkerDTO.builder()
                .id(w.getId())
                .schoolYearId(w.getSchoolYearId())
                .workerId(w.getWorkerId())
                .roleId(w.getRoleId())
                .normId(w.getNormId())
                .build();
    }
}
