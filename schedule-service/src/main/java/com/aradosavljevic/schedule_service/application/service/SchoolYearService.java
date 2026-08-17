package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.SchoolYearDTO;
import com.aradosavljevic.schedule_service.application.request.teaching.SchoolYearCreateRequest;
import org.springframework.data.domain.Pageable;

public interface SchoolYearService {

    PageResponse<SchoolYearDTO> getAll(Pageable pageable);

    SchoolYearDTO getById(Long id);

    SchoolYearDTO create(SchoolYearCreateRequest request);

    void delete(Long id);
}
