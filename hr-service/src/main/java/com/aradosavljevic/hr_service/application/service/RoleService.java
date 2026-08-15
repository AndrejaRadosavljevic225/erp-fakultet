package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.RoleDTO;
import com.aradosavljevic.hr_service.application.request.role.RoleCreateRequest;
import com.aradosavljevic.hr_service.application.request.role.RoleUpdateRequest;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    PageResponse<RoleDTO> getAll(Pageable pageable);

    RoleDTO getById(Long id);

    RoleDTO create(RoleCreateRequest request);

    RoleDTO update(Long id, RoleUpdateRequest request);

    void delete(Long id);
}
