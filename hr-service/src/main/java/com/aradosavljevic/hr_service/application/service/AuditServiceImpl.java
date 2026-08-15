package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.hr_service.application.dto.AuditLogDTO;
import com.aradosavljevic.hr_service.application.mapper.AuditMapper;
import com.aradosavljevic.hr_service.application.mapper.PageMapper;
import com.aradosavljevic.hr_service.domain.entity.AuditLog;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.repository.AuditLogRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditMapper auditMapper;

    @Override
    @Transactional
    public void log(String entityName, Long entityId, String action, String details) {
        AuditLog entry = new AuditLog();
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setDetails(details);
        entry.setChangedBy(currentUserId());
        auditLogRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDTO> getAll(Pageable pageable) {
        return PageMapper.toPageResponse(
                auditLogRepository.findAllByOrderByChangedAtDesc(pageable), auditMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getForEntity(String entityName, Long entityId) {
        return auditLogRepository
                .findByEntityNameAndEntityIdOrderByChangedAtDesc(entityName, entityId).stream()
                .map(auditMapper::toDTO)
                .toList();
    }

    private Long currentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return userAccountRepository.findByUsername(auth.getName())
                        .map(UserAccount::getId)
                        .orElse(null);
            }
        } catch (Exception ignored) {
            // bez konteksta (npr. registracija) -> changedBy ostaje null
        }
        return null;
    }
}
