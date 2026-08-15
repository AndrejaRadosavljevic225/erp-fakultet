package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.hr_service.application.dto.WorkerPositionDTO;
import com.aradosavljevic.hr_service.application.request.assignment.WorkerPositionAssignRequest;
import com.aradosavljevic.hr_service.application.request.assignment.WorkerPositionUpdateRequest;

import java.util.List;

public interface WorkerPositionService {

    List<WorkerPositionDTO> getByWorker(Long workerId);

    WorkerPositionDTO assign(WorkerPositionAssignRequest request);

    WorkerPositionDTO update(Long id, WorkerPositionUpdateRequest request);

    void remove(Long id);
}
