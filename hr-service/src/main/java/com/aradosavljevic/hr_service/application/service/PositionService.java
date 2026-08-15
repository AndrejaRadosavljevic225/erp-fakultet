package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.PositionDTO;
import com.aradosavljevic.hr_service.application.request.position.PositionCreateRequest;
import com.aradosavljevic.hr_service.application.request.position.PositionUpdateRequest;
import org.springframework.data.domain.Pageable;

public interface PositionService {

    PageResponse<PositionDTO> getAll(Pageable pageable);

    PositionDTO getById(Long id);

    PositionDTO create(PositionCreateRequest request);

    PositionDTO update(Long id, PositionUpdateRequest request);

    void delete(Long id);
}
