package com.aradosavljevic.hr_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.hr_service.application.dto.PositionDTO;
import com.aradosavljevic.hr_service.application.mapper.PageMapper;
import com.aradosavljevic.hr_service.application.mapper.PositionMapper;
import com.aradosavljevic.hr_service.application.request.position.PositionCreateRequest;
import com.aradosavljevic.hr_service.application.request.position.PositionUpdateRequest;
import com.aradosavljevic.hr_service.domain.entity.Position;
import com.aradosavljevic.hr_service.domain.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PositionDTO> getAll(Pageable pageable) {
        return PageMapper.toPageResponse(positionRepository.findAll(pageable), positionMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDTO getById(Long id) {
        return positionRepository.findById(id)
                .map(positionMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));
    }

    @Override
    @Transactional
    public PositionDTO create(PositionCreateRequest request) {
        Position position = new Position();
        position.setTitle(request.getTitle());
        position.setSalaryGrade(request.getSalaryGrade());
        position.setBaseSalary(request.getBaseSalary());
        position.setIsVacant(request.getIsVacant() != null ? request.getIsVacant() : true);
        return positionMapper.toDTO(positionRepository.save(position));
    }

    @Override
    @Transactional
    public PositionDTO update(Long id, PositionUpdateRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));

        if (request.getTitle() != null) position.setTitle(request.getTitle());
        if (request.getSalaryGrade() != null) position.setSalaryGrade(request.getSalaryGrade());
        if (request.getBaseSalary() != null) position.setBaseSalary(request.getBaseSalary());
        if (request.getIsVacant() != null) position.setIsVacant(request.getIsVacant());

        return positionMapper.toDTO(positionRepository.save(position));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Position", "id", id);
        }
        positionRepository.deleteById(id);
    }
}
