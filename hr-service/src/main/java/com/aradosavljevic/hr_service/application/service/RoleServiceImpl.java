package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.hr_service.application.dto.RoleDTO;
import com.aradosavljevic.hr_service.application.mapper.PageMapper;
import com.aradosavljevic.hr_service.application.mapper.RoleMapper;
import com.aradosavljevic.hr_service.application.request.role.RoleCreateRequest;
import com.aradosavljevic.hr_service.application.request.role.RoleUpdateRequest;
import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleDTO> getAll(Pageable pageable) {
        return PageMapper.toPageResponse(roleRepository.findAll(pageable), roleMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    @Transactional
    public RoleDTO create(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Rola sa kodom '" + request.getCode() + "' vec postoji");
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setIsActive(true);
        role.setCreatedAt(LocalDateTime.now());
        return roleMapper.toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleDTO update(Long id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (request.getName() != null) role.setName(request.getName());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        if (request.getIsActive() != null) role.setIsActive(request.getIsActive());

        return roleMapper.toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role", "id", id);
        }
        roleRepository.deleteById(id);
    }
}
