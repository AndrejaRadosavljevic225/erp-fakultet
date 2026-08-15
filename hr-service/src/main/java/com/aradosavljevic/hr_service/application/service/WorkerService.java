package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.WorkerDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerDetailDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerSummaryDTO;
import com.aradosavljevic.hr_service.application.request.worker.WorkerCreateRequest;
import com.aradosavljevic.hr_service.application.request.worker.WorkerUpdateRequest;
import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkerService {

    PageResponse<WorkerSummaryDTO> search(String searchTerm, Pageable pageable);

    List<WorkerSummaryDTO> getByStatus(EmploymentStatus status);

    WorkerDetailDTO getById(Long id);

    WorkerDTO create(WorkerCreateRequest request);

    WorkerDTO update(Long id, WorkerUpdateRequest request);

    void delete(Long id);
}
