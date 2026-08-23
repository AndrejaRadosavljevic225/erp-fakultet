package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.hr_service.application.dto.AuthResponse;
import com.aradosavljevic.hr_service.application.dto.CurrentUserDTO;
import com.aradosavljevic.hr_service.application.dto.PermissionDTO;
import com.aradosavljevic.hr_service.application.request.auth.ChangePasswordRequest;
import com.aradosavljevic.hr_service.application.request.auth.LoginRequest;
import com.aradosavljevic.hr_service.application.request.auth.RegisterRequest;
import com.aradosavljevic.hr_service.domain.entity.LoginLog;
import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.repository.LoginLogRepository;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerRepository;
import com.aradosavljevic.hr_service.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RoleRepository roleRepository;
    private final WorkerRepository workerRepository;
    private final RolePermissionService rolePermissionService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username '" + request.getUsername() + "' je vec zauzet");
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setWorkerId(request.getWorkerId());
        user.setRoleId(request.getRoleId());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        UserAccount saved = userAccountRepository.save(user);

        String token = tokenProvider.generateToken(saved.getUsername(), saved.getId(), saved.getRoleId(),
                roleCode(saved.getRoleId()), saved.getWorkerId());
        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .username(saved.getUsername())
                .roleId(saved.getRoleId())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new BusinessException("Neispravno korisnicko ime ili lozinka"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Neispravno korisnicko ime ili lozinka");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessException("Korisnicki nalog je deaktiviran");
        }

        user.setLastLogin(LocalDateTime.now());
        userAccountRepository.save(user);

        LoginLog log = new LoginLog();
        log.setUserId(user.getId());
        log.setLoginTime(LocalDateTime.now());
        log.setStatus("LOGIN");
        loginLogRepository.save(log);

        String token = tokenProvider.generateToken(user.getUsername(), user.getId(), user.getRoleId(),
                roleCode(user.getRoleId()), user.getWorkerId());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .roleId(user.getRoleId())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Korisnik nije pronadjen"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Stara lozinka nije ispravna");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserDTO getCurrentUser(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Korisnik nije pronadjen"));

        Role role = user.getRoleId() != null
                ? roleRepository.findById(user.getRoleId()).orElse(null)
                : null;
        Worker worker = user.getWorkerId() != null
                ? workerRepository.findById(user.getWorkerId()).orElse(null)
                : null;
        List<String> permissions = rolePermissionService.getPermissionsOfUser(user.getId())
                .stream().map(PermissionDTO::getCode).toList();

        return CurrentUserDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .roleId(user.getRoleId())
                .roleCode(role != null ? role.getCode() : null)
                .roleName(role != null ? role.getName() : null)
                .workerId(user.getWorkerId())
                .workerFullName(worker != null ? worker.getFullName() : null)
                .workerEmail(worker != null ? worker.getEmail() : null)
                .permissions(permissions)
                .build();
    }

    /**
     * Prijava korisnickim imenom ili email-om zaposlenog: prvo se trazi nalog po username-u,
     * pa (ako nema pogotka i unos lici na email) worker po email-u pa njegov nalog.
     */
    private Optional<UserAccount> findByUsernameOrEmail(String input) {
        Optional<UserAccount> byUsername = userAccountRepository.findByUsername(input);
        if (byUsername.isPresent() || input == null || !input.contains("@")) {
            return byUsername;
        }
        return workerRepository.findByEmail(input)
                .flatMap(w -> userAccountRepository.findByWorkerId(w.getId()));
    }

    private String roleCode(Long roleId) {
        if (roleId == null) return null;
        return roleRepository.findById(roleId).map(Role::getCode).orElse(null);
    }
}
