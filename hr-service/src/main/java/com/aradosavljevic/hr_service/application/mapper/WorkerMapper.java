package com.aradosavljevic.hr_service.application.mapper;

import com.aradosavljevic.hr_service.application.dto.WorkerDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerDetailDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerPositionDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerSummaryDTO;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.entity.WorkerPosition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkerMapper {

    public WorkerDTO toDTO(Worker w) {
        if (w == null) return null;
        return WorkerDTO.builder()
                .id(w.getId())
                .firstName(w.getFirstName())
                .lastName(w.getLastName())
                .fullName(w.getFullName())
                .email(w.getEmail())
                .personalId(w.getPersonalId())
                .phone(w.getPhone())
                .hireDate(w.getHireDate())
                .terminationDate(w.getTerminationDate())
                .employmentStatus(w.getEmploymentStatus())
                .employmentType(w.getEmploymentType())
                .active(w.isActive())
                .build();
    }

    public WorkerSummaryDTO toSummary(Worker w) {
        if (w == null) return null;
        return WorkerSummaryDTO.builder()
                .id(w.getId())
                .fullName(w.getFullName())
                .email(w.getEmail())
                .employmentStatus(w.getEmploymentStatus())
                .build();
    }

    public WorkerPositionDTO toPositionDTO(WorkerPosition wp, String positionTitle) {
        if (wp == null) return null;
        return WorkerPositionDTO.builder()
                .id(wp.getId())
                .workerId(wp.getWorkerId())
                .positionId(wp.getPositionId())
                .positionTitle(positionTitle)
                .validFrom(wp.getValidFrom())
                .validTo(wp.getValidTo())
                .fraction(wp.getFraction())
                .isPrimary(wp.getIsPrimary())
                .active(wp.isActive())
                .build();
    }

    public WorkerDetailDTO toDetailDTO(Worker w, List<WorkerPositionDTO> positions) {
        if (w == null) return null;
        return WorkerDetailDTO.builder()
                .id(w.getId())
                .firstName(w.getFirstName())
                .lastName(w.getLastName())
                .fullName(w.getFullName())
                .email(w.getEmail())
                .personalId(w.getPersonalId())
                .phone(w.getPhone())
                .hireDate(w.getHireDate())
                .terminationDate(w.getTerminationDate())
                .employmentStatus(w.getEmploymentStatus())
                .employmentType(w.getEmploymentType())
                .active(w.isActive())
                .positions(positions)
                .build();
    }
}
