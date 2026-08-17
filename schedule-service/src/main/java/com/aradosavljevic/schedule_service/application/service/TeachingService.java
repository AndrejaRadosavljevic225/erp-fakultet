package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.SchoolYearWorkerDTO;
import com.aradosavljevic.schedule_service.application.dto.TeachingNormDTO;
import com.aradosavljevic.schedule_service.application.dto.TeachingReportDTO;
import com.aradosavljevic.schedule_service.application.request.teaching.SchoolYearWorkerRequest;
import com.aradosavljevic.schedule_service.application.request.teaching.TeachingNormCreateRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeachingService {

    // --- Norme (kvote) ---
    PageResponse<TeachingNormDTO> getAllNorms(Pageable pageable);

    TeachingNormDTO createNorm(TeachingNormCreateRequest request);

    void deleteNorm(Long id);

    // --- Dodela profesora godini ---
    List<SchoolYearWorkerDTO> getAssignmentsByYear(Long schoolYearId);

    SchoolYearWorkerDTO assignWorker(SchoolYearWorkerRequest request);

    void removeAssignment(Long id);

    // --- Fond casova (izvestaj) ---
    TeachingReportDTO report(Long workerId, Long schoolYearId);

    List<TeachingReportDTO> reportByYear(Long schoolYearId);
}
