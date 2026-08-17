package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.schedule_service.application.dto.SchoolYearDTO;
import com.aradosavljevic.schedule_service.application.mapper.PageMapper;
import com.aradosavljevic.schedule_service.application.mapper.SchoolYearMapper;
import com.aradosavljevic.schedule_service.application.request.teaching.SchoolYearCreateRequest;
import com.aradosavljevic.schedule_service.domain.entity.SchoolYear;
import com.aradosavljevic.schedule_service.domain.repository.SchoolYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchoolYearServiceImpl implements SchoolYearService {

    private final SchoolYearRepository schoolYearRepository;
    private final SchoolYearMapper schoolYearMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SchoolYearDTO> getAll(Pageable pageable) {
        return PageMapper.toPageResponse(schoolYearRepository.findAll(pageable), schoolYearMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolYearDTO getById(Long id) {
        return schoolYearRepository.findById(id)
                .map(schoolYearMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("SchoolYear", "id", id));
    }

    @Override
    @Transactional
    public SchoolYearDTO create(SchoolYearCreateRequest request) {
        if (schoolYearRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Skolska godina sa kodom '" + request.getCode() + "' vec postoji");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("Datum kraja ne moze biti pre datuma pocetka");
        }
        SchoolYear sy = new SchoolYear();
        sy.setCode(request.getCode());
        sy.setStartDate(request.getStartDate());
        sy.setEndDate(request.getEndDate());
        sy.setDescription(request.getDescription());
        return schoolYearMapper.toDTO(schoolYearRepository.save(sy));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!schoolYearRepository.existsById(id)) {
            throw new ResourceNotFoundException("SchoolYear", "id", id);
        }
        schoolYearRepository.deleteById(id);
    }
}
