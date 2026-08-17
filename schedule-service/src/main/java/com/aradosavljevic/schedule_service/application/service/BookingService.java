package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.AvailabilityResponse;
import com.aradosavljevic.schedule_service.application.dto.BookingDTO;
import com.aradosavljevic.schedule_service.application.request.booking.AvailabilityRequest;
import com.aradosavljevic.schedule_service.application.request.booking.BookingCreateRequest;
import com.aradosavljevic.schedule_service.application.request.booking.BookingUpdateRequest;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    BookingDTO getById(Long id);

    PageResponse<BookingDTO> getByRoom(Long roomId, Pageable pageable);

    PageResponse<BookingDTO> getByWorker(Long workerId, Pageable pageable);

    PageResponse<BookingDTO> getByStatus(BookingStatus status, Pageable pageable);

    List<BookingDTO> occupancy(Long roomId, LocalDateTime from, LocalDateTime to);

    AvailabilityResponse checkAvailability(AvailabilityRequest request);

    BookingDTO create(BookingCreateRequest request);

    BookingDTO update(Long id, BookingUpdateRequest request);

    BookingDTO approve(Long id, Long approvedBy);

    BookingDTO reject(Long id, Long approvedBy);

    BookingDTO cancel(Long id);
}
