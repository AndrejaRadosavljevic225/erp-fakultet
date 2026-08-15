package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.PermissionDTO;
import com.aradosavljevic.hr_service.application.request.permission.PermissionCreateRequest;
import org.springframework.data.domain.Pageable;

public interface PermissionService {

    PageResponse<PermissionDTO> getAll(Pageable pageable);

    PermissionDTO getById(Long id);

    PermissionDTO create(PermissionCreateRequest request);

    void delete(Long id);
}
