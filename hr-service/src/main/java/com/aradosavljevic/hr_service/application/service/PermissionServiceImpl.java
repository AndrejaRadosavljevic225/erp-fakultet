package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.hr_service.application.dto.PermissionDTO;
import com.aradosavljevic.hr_service.application.mapper.PageMapper;
import com.aradosavljevic.hr_service.application.mapper.PermissionMapper;
import com.aradosavljevic.hr_service.application.request.permission.PermissionCreateRequest;
import com.aradosavljevic.hr_service.domain.entity.Permission;
import com.aradosavljevic.hr_service.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionDTO> getAll(Pageable pageable) {
        return PageMapper.toPageResponse(permissionRepository.findAll(pageable), permissionMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getById(Long id) {
        return permissionRepository.findById(id)
                .map(permissionMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }

    @Override
    @Transactional
    public PermissionDTO create(PermissionCreateRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Permisija sa kodom '" + request.getCode() + "' vec postoji");
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setModule(request.getModule());
        Permission saved = permissionRepository.save(permission);
        auditService.log("Permission", saved.getId(), "CREATE", "code=" + saved.getCode());
        return permissionMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission", "id", id);
        }
        permissionRepository.deleteById(id);
        auditService.log("Permission", id, "DELETE", null);
    }
}
