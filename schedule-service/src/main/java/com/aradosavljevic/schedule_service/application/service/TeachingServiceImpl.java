package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.schedule_service.application.dto.SchoolYearWorkerDTO;
import com.aradosavljevic.schedule_service.application.dto.TeachingNormDTO;
import com.aradosavljevic.schedule_service.application.dto.TeachingReportDTO;
import com.aradosavljevic.schedule_service.application.mapper.BookingMapper;
import com.aradosavljevic.schedule_service.application.mapper.PageMapper;
import com.aradosavljevic.schedule_service.application.mapper.TeachingMapper;
import com.aradosavljevic.schedule_service.application.request.teaching.SchoolYearWorkerRequest;
import com.aradosavljevic.schedule_service.application.request.teaching.TeachingNormCreateRequest;
import com.aradosavljevic.schedule_service.domain.entity.Booking;
import com.aradosavljevic.schedule_service.domain.entity.SchoolYearWorker;
import com.aradosavljevic.schedule_service.domain.entity.TeachingNorm;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import com.aradosavljevic.schedule_service.domain.repository.BookingRepository;
import com.aradosavljevic.schedule_service.domain.repository.SchoolYearRepository;
import com.aradosavljevic.schedule_service.domain.repository.SchoolYearWorkerRepository;
import com.aradosavljevic.schedule_service.domain.repository.TeachingNormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeachingServiceImpl implements TeachingService {

    /** Termini koji se broje kao "odradjeni". */
    private static final List<BookingStatus> DONE_STATUSES =
            List.of(BookingStatus.ACCEPTED, BookingStatus.FINISHED);

    private final TeachingNormRepository teachingNormRepository;
    private final SchoolYearWorkerRepository schoolYearWorkerRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final BookingRepository bookingRepository;
    private final TeachingMapper teachingMapper;

    // --- Norme ---

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeachingNormDTO> getAllNorms(Pageable pageable) {
        return PageMapper.toPageResponse(teachingNormRepository.findAll(pageable), teachingMapper::toNormDTO);
    }

    @Override
    @Transactional
    public TeachingNormDTO createNorm(TeachingNormCreateRequest request) {
        teachingNormRepository.findByRoleIdAndSchoolYearId(request.getRoleId(), request.getSchoolYearId())
                .ifPresent(n -> {
                    throw new BusinessException("Norma za tu rolu i godinu vec postoji");
                });
        TeachingNorm norm = new TeachingNorm();
        norm.setRoleId(request.getRoleId());
        norm.setSchoolYearId(request.getSchoolYearId());
        norm.setRequiredHours(request.getRequiredHours());
        norm.setDescription(request.getDescription());
        return teachingMapper.toNormDTO(teachingNormRepository.save(norm));
    }

    @Override
    @Transactional
    public void deleteNorm(Long id) {
        if (!teachingNormRepository.existsById(id)) {
            throw new ResourceNotFoundException("TeachingNorm", "id", id);
        }
        teachingNormRepository.deleteById(id);
    }

    // --- Dodela profesora godini ---

    @Override
    @Transactional(readOnly = true)
    public List<SchoolYearWorkerDTO> getAssignmentsByYear(Long schoolYearId) {
        return schoolYearWorkerRepository.findBySchoolYearId(schoolYearId).stream()
                .map(teachingMapper::toWorkerDTO)
                .toList();
    }

    @Override
    @Transactional
    public SchoolYearWorkerDTO assignWorker(SchoolYearWorkerRequest request) {
        if (!schoolYearRepository.existsById(request.getSchoolYearId())) {
            throw new ResourceNotFoundException("SchoolYear", "id", request.getSchoolYearId());
        }
        if (schoolYearWorkerRepository.existsByWorkerIdAndSchoolYearId(request.getWorkerId(), request.getSchoolYearId())) {
            throw new BusinessException("Profesor je vec dodeljen ovoj skolskoj godini");
        }

        Long normId = request.getNormId();
        if (normId == null && request.getRoleId() != null) {
            normId = teachingNormRepository
                    .findByRoleIdAndSchoolYearId(request.getRoleId(), request.getSchoolYearId())
                    .map(TeachingNorm::getId)
                    .orElse(null);
        }

        SchoolYearWorker syw = new SchoolYearWorker();
        syw.setSchoolYearId(request.getSchoolYearId());
        syw.setWorkerId(request.getWorkerId());
        syw.setRoleId(request.getRoleId());
        syw.setNormId(normId);
        return teachingMapper.toWorkerDTO(schoolYearWorkerRepository.save(syw));
    }

    @Override
    @Transactional
    public void removeAssignment(Long id) {
        if (!schoolYearWorkerRepository.existsById(id)) {
            throw new ResourceNotFoundException("SchoolYearWorker", "id", id);
        }
        schoolYearWorkerRepository.deleteById(id);
    }

    // --- Fond casova (izvestaj) ---

    @Override
    @Transactional(readOnly = true)
    public TeachingReportDTO report(Long workerId, Long schoolYearId) {
        SchoolYearWorker assignment = schoolYearWorkerRepository
                .findByWorkerIdAndSchoolYearId(workerId, schoolYearId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor " + workerId + " nije dodeljen skolskoj godini " + schoolYearId));
        return buildReport(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingReportDTO> reportByYear(Long schoolYearId) {
        return schoolYearWorkerRepository.findBySchoolYearId(schoolYearId).stream()
                .map(this::buildReport)
                .toList();
    }

    private TeachingReportDTO buildReport(SchoolYearWorker assignment) {
        int required = resolveRequiredHours(assignment);
        double realized = realizedHours(assignment.getWorkerId(), assignment.getSchoolYearId());
        double deviation = Math.round((realized - required) * 100.0) / 100.0;

        return TeachingReportDTO.builder()
                .workerId(assignment.getWorkerId())
                .schoolYearId(assignment.getSchoolYearId())
                .requiredHours(required)
                .realizedHours(realized)
                .deviation(deviation)
                .extraHours(Math.max(0, deviation))
                .fulfilled(realized >= required)
                .build();
    }

    /** Kvota iz povezane norme; ako nema norm_id, pokusaj po roli+godini; inace 0. */
    private int resolveRequiredHours(SchoolYearWorker assignment) {
        TeachingNorm norm = null;
        if (assignment.getNormId() != null) {
            norm = teachingNormRepository.findById(assignment.getNormId()).orElse(null);
        }
        if (norm == null && assignment.getRoleId() != null) {
            norm = teachingNormRepository
                    .findByRoleIdAndSchoolYearId(assignment.getRoleId(), assignment.getSchoolYearId())
                    .orElse(null);
        }
        return (norm != null && norm.getRequiredHours() != null) ? norm.getRequiredHours() : 0;
    }

    /** Zbir trajanja odrzanih NASTAVNIH termina profesora u toj godini. */
    private double realizedHours(Long workerId, Long schoolYearId) {
        double sum = bookingRepository.findByRequesterWorkerIdAndSchoolYearId(workerId, schoolYearId).stream()
                .filter(b -> b.getTeachingType() != null)
                .filter(b -> DONE_STATUSES.contains(b.getStatus()))
                .mapToDouble(BookingMapper::durationHours)
                .sum();
        return Math.round(sum * 100.0) / 100.0;
    }
}
