package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.hr_service.application.dto.WorkerPositionDTO;
import com.aradosavljevic.hr_service.application.mapper.WorkerMapper;
import com.aradosavljevic.hr_service.application.request.assignment.WorkerPositionAssignRequest;
import com.aradosavljevic.hr_service.application.request.assignment.WorkerPositionUpdateRequest;
import com.aradosavljevic.hr_service.domain.entity.Position;
import com.aradosavljevic.hr_service.domain.entity.WorkerPosition;
import com.aradosavljevic.hr_service.domain.repository.PositionRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerPositionRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerPositionServiceImpl implements WorkerPositionService {

    private final WorkerPositionRepository workerPositionRepository;
    private final WorkerRepository workerRepository;
    private final PositionRepository positionRepository;
    private final WorkerMapper workerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WorkerPositionDTO> getByWorker(Long workerId) {
        if (!workerRepository.existsById(workerId)) {
            throw new ResourceNotFoundException("Worker", "id", workerId);
        }
        List<WorkerPosition> wps = workerPositionRepository.findByWorkerId(workerId);
        Map<Long, String> titles = positionRepository.findAllById(
                        wps.stream().map(WorkerPosition::getPositionId).filter(pid -> pid != null).distinct().toList())
                .stream().collect(Collectors.toMap(Position::getId, Position::getTitle));
        return wps.stream()
                .map(wp -> workerMapper.toPositionDTO(wp, titles.get(wp.getPositionId())))
                .toList();
    }

    @Override
    @Transactional
    public WorkerPositionDTO assign(WorkerPositionAssignRequest request) {
        if (!workerRepository.existsById(request.getWorkerId())) {
            throw new ResourceNotFoundException("Worker", "id", request.getWorkerId());
        }
        if (!positionRepository.existsById(request.getPositionId())) {
            throw new ResourceNotFoundException("Position", "id", request.getPositionId());
        }

        WorkerPosition wp = new WorkerPosition();
        wp.setWorkerId(request.getWorkerId());
        wp.setPositionId(request.getPositionId());
        wp.setValidFrom(request.getValidFrom());
        wp.setValidTo(request.getValidTo());
        wp.setFraction(request.getFraction() != null ? request.getFraction() : BigDecimal.ONE);
        wp.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);

        WorkerPosition saved = workerPositionRepository.save(wp);
        return workerMapper.toPositionDTO(saved, positionTitle(saved.getPositionId()));
    }

    @Override
    @Transactional
    public WorkerPositionDTO update(Long id, WorkerPositionUpdateRequest request) {
        WorkerPosition wp = workerPositionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkerPosition", "id", id));

        if (request.getValidFrom() != null) wp.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) wp.setValidTo(request.getValidTo());
        if (request.getFraction() != null) wp.setFraction(request.getFraction());
        if (request.getIsPrimary() != null) wp.setIsPrimary(request.getIsPrimary());

        WorkerPosition saved = workerPositionRepository.save(wp);
        return workerMapper.toPositionDTO(saved, positionTitle(saved.getPositionId()));
    }

    @Override
    @Transactional
    public void remove(Long id) {
        if (!workerPositionRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkerPosition", "id", id);
        }
        workerPositionRepository.deleteById(id);
    }

    private String positionTitle(Long positionId) {
        return positionRepository.findById(positionId)
                .map(Position::getTitle)
                .orElse(null);
    }
}
