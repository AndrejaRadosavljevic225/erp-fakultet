package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.hr_service.application.dto.UserDTO;
import com.aradosavljevic.hr_service.application.mapper.PageMapper;
import com.aradosavljevic.hr_service.application.mapper.UserMapper;
import com.aradosavljevic.hr_service.application.request.user.UserCreateRequest;
import com.aradosavljevic.hr_service.application.request.user.UserUpdateRequest;
import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getAll(Pageable pageable) {
        return PageMapper.toPageResponse(userAccountRepository.findAll(pageable),
                u -> userMapper.toDTO(u, roleName(u.getRoleId())));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user, roleName(user.getRoleId()));
    }

    @Override
    @Transactional
    public UserDTO create(UserCreateRequest request) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username '" + request.getUsername() + "' je vec zauzet");
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setWorkerId(request.getWorkerId());
        user.setRoleId(request.getRoleId());
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setCreatedAt(LocalDateTime.now());
        UserAccount saved = userAccountRepository.save(user);
        return userMapper.toDTO(saved, roleName(saved.getRoleId()));
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserUpdateRequest request) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getWorkerId() != null) user.setWorkerId(request.getWorkerId());
        if (request.getRoleId() != null) user.setRoleId(request.getRoleId());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());

        UserAccount saved = userAccountRepository.save(user);
        return userMapper.toDTO(saved, roleName(saved.getRoleId()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userAccountRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userAccountRepository.deleteById(id);
    }

    private String roleName(Long roleId) {
        if (roleId == null) return null;
        return roleRepository.findById(roleId).map(Role::getName).orElse(null);
    }
}
