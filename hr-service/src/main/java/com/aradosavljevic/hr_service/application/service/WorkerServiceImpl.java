package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.hr_service.application.dto.WorkerDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerDetailDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerPositionDTO;
import com.aradosavljevic.hr_service.application.dto.WorkerSummaryDTO;
import com.aradosavljevic.hr_service.application.mapper.PageMapper;
import com.aradosavljevic.hr_service.application.mapper.WorkerMapper;
import com.aradosavljevic.hr_service.application.request.worker.WorkerCreateRequest;
import com.aradosavljevic.hr_service.application.request.worker.WorkerUpdateRequest;
import com.aradosavljevic.hr_service.domain.entity.Position;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.entity.WorkerPosition;
import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.repository.PositionRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerPositionRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;
    private final WorkerPositionRepository workerPositionRepository;
    private final PositionRepository positionRepository;
    private final WorkerMapper workerMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkerSummaryDTO> search(String searchTerm, Pageable pageable) {
        Page<Worker> page = StringUtils.hasText(searchTerm)
                ? workerRepository.searchWorkers(searchTerm, pageable)
                : workerRepository.findAll(pageable);
        return PageMapper.toPageResponse(page, workerMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerSummaryDTO> getByStatus(EmploymentStatus status) {
        return workerRepository.findByEmploymentStatus(status).stream()
                .map(workerMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkerDetailDTO getById(Long id) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", id));

        List<WorkerPosition> workerPositions = workerPositionRepository.findByWorkerId(id);
        Map<Long, String> titles = positionTitles(workerPositions.stream()
                .map(WorkerPosition::getPositionId).toList());
        List<WorkerPositionDTO> positions = workerPositions.stream()
                .map(wp -> workerMapper.toPositionDTO(wp, titles.get(wp.getPositionId())))
                .toList();

        return workerMapper.toDetailDTO(worker, positions);
    }

    @Override
    @Transactional
    public WorkerDTO create(WorkerCreateRequest request) {
        if (workerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Radnik sa email-om '" + request.getEmail() + "' vec postoji");
        }
        if (workerRepository.findByPersonalId(request.getPersonalId()).isPresent()) {
            throw new BusinessException("Radnik sa JMBG-om '" + request.getPersonalId() + "' vec postoji");
        }

        Worker worker = new Worker();
        worker.setFirstName(request.getFirstName());
        worker.setLastName(request.getLastName());
        worker.setEmail(request.getEmail());
        worker.setPersonalId(request.getPersonalId());
        worker.setPhone(request.getPhone());
        worker.setHireDate(request.getHireDate());
        worker.setEmploymentStatus(request.getEmploymentStatus() != null
                ? request.getEmploymentStatus() : EmploymentStatus.ACTIVE);
        worker.setEmploymentType(request.getEmploymentType());

        Worker saved = workerRepository.save(worker);
        auditService.log("Worker", saved.getId(), "CREATE", "email=" + saved.getEmail());
        return workerMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public WorkerDTO update(Long id, WorkerUpdateRequest request) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", id));

        if (request.getFirstName() != null) worker.setFirstName(request.getFirstName());
        if (request.getLastName() != null) worker.setLastName(request.getLastName());
        if (request.getEmail() != null) worker.setEmail(request.getEmail());
        if (request.getPersonalId() != null) worker.setPersonalId(request.getPersonalId());
        if (request.getPhone() != null) worker.setPhone(request.getPhone());
        if (request.getHireDate() != null) worker.setHireDate(request.getHireDate());
        if (request.getTerminationDate() != null) worker.setTerminationDate(request.getTerminationDate());
        if (request.getEmploymentStatus() != null) worker.setEmploymentStatus(request.getEmploymentStatus());
        if (request.getEmploymentType() != null) worker.setEmploymentType(request.getEmploymentType());

        Worker saved = workerRepository.save(worker);
        auditService.log("Worker", saved.getId(), "UPDATE", "status=" + saved.getEmploymentStatus());
        return workerMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!workerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Worker", "id", id);
        }
        workerPositionRepository.findByWorkerId(id)
                .forEach(workerPositionRepository::delete);
        workerRepository.deleteById(id);
        auditService.log("Worker", id, "DELETE", null);
    }

    private Map<Long, String> positionTitles(List<Long> positionIds) {
        return positionRepository.findAllById(
                        positionIds.stream().filter(pid -> pid != null).distinct().toList()).stream()
                .collect(Collectors.toMap(Position::getId, Position::getTitle));
    }
}
