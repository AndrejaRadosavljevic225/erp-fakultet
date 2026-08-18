package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.hr_service.application.dto.PermissionDTO;
import com.aradosavljevic.hr_service.application.mapper.PermissionMapper;
import com.aradosavljevic.hr_service.domain.entity.Permission;
import com.aradosavljevic.hr_service.domain.entity.RolePermission;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.repository.PermissionRepository;
import com.aradosavljevic.hr_service.domain.repository.RolePermissionRepository;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final PermissionMapper permissionMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsOfRole(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role", "id", roleId);
        }
        List<Long> permissionIds = rolePermissionRepository.findByRoleId(roleId).stream()
                .map(RolePermission::getPermissionId)
                .filter(pid -> pid != null)
                .distinct()
                .toList();
        return permissionRepository.findAllById(permissionIds).stream()
                .map(permissionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsOfUser(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRoleId() == null) {
            return Collections.emptyList();
        }
        return getPermissionsOfRole(user.getRoleId());
    }

    @Override
    @Transactional
    public PermissionDTO assignPermissionToRole(Long roleId, Long permissionId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role", "id", roleId);
        }
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        if (rolePermissionRepository.existsByRoleAndPermission(roleId, permissionId)) {
            throw new BusinessException("Permisija je vec dodeljena ovoj roli");
        }

        RolePermission rp = new RolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        rolePermissionRepository.save(rp);

        auditService.log("Role", roleId, "PERMISSION_ASSIGNED", "permissionId=" + permissionId);
        return permissionMapper.toDTO(permission);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        RolePermission link = rolePermissionRepository.findByRoleId(roleId).stream()
                .filter(rp -> permissionId.equals(rp.getPermissionId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Veza role " + roleId + " i permisije " + permissionId + " ne postoji"));

        rolePermissionRepository.delete(link);
        auditService.log("Role", roleId, "PERMISSION_REMOVED", "permissionId=" + permissionId);
    }
}
