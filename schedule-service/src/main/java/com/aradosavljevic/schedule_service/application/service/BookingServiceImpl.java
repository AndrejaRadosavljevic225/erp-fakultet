package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.BusinessException;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.schedule_service.application.dto.AvailabilityResponse;
import com.aradosavljevic.schedule_service.application.dto.BookingDTO;
import com.aradosavljevic.schedule_service.application.mapper.BookingMapper;
import com.aradosavljevic.schedule_service.application.mapper.PageMapper;
import com.aradosavljevic.schedule_service.application.request.booking.AvailabilityRequest;
import com.aradosavljevic.schedule_service.application.request.booking.BookingCreateRequest;
import com.aradosavljevic.schedule_service.application.request.booking.BookingUpdateRequest;
import com.aradosavljevic.schedule_service.domain.entity.Booking;
import com.aradosavljevic.schedule_service.domain.entity.Room;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import com.aradosavljevic.schedule_service.domain.repository.BookingRepository;
import com.aradosavljevic.schedule_service.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    /** Statusi koji "drze" termin i blokiraju preklapanje. */
    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.REQUESTED, BookingStatus.ACCEPTED);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional(readOnly = true)
    public BookingDTO getById(Long id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return toDTO(b);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingDTO> getByRoom(Long roomId, Pageable pageable) {
        var page = bookingRepository.findByRoomId(roomId, pageable);
        return PageMapper.toPageResponse(page, toDTOs(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingDTO> getByWorker(Long workerId, Pageable pageable) {
        var page = bookingRepository.findByRequesterWorkerId(workerId, pageable);
        return PageMapper.toPageResponse(page, toDTOs(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingDTO> getByStatus(BookingStatus status, Pageable pageable) {
        var page = bookingRepository.findByStatus(status, pageable);
        return PageMapper.toPageResponse(page, toDTOs(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> occupancy(Long roomId, LocalDateTime from, LocalDateTime to) {
        return toDTOs(bookingRepository.findByRoomIdAndStartDateTimeBetween(roomId, from, to));
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        List<Booking> conflicts = bookingRepository.findOverlapping(
                request.getRoomId(), request.getStartDateTime(), request.getEndDateTime(), ACTIVE_STATUSES);
        return AvailabilityResponse.builder()
                .roomId(request.getRoomId())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .available(conflicts.isEmpty())
                .conflicts(toDTOs(conflicts))
                .build();
    }

    @Override
    @Transactional
    public BookingDTO create(BookingCreateRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));
        if (!Boolean.TRUE.equals(room.getBookable()) || !Boolean.TRUE.equals(room.getActive())) {
            throw new BusinessException("Prostorija nije dostupna za rezervaciju");
        }
        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new BusinessException("Kraj termina mora biti posle pocetka");
        }
        List<Booking> conflicts = bookingRepository.findOverlapping(
                request.getRoomId(), request.getStartDateTime(), request.getEndDateTime(), ACTIVE_STATUSES);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Termin je zauzet u izabranoj prostoriji");
        }

        Booking b = new Booking();
        b.setRoomId(request.getRoomId());
        b.setRequesterWorkerId(request.getRequesterWorkerId());
        b.setSchoolYearId(request.getSchoolYearId());
        b.setStartDateTime(request.getStartDateTime());
        b.setEndDateTime(request.getEndDateTime());
        b.setPurpose(request.getPurpose());
        b.setTeachingType(request.getTeachingType());
        b.setStatus(BookingStatus.REQUESTED);

        return toDTO(bookingRepository.save(b));
    }

    @Override
    @Transactional
    public BookingDTO update(Long id, BookingUpdateRequest request) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        LocalDateTime newStart = request.getStartDateTime() != null ? request.getStartDateTime() : b.getStartDateTime();
        LocalDateTime newEnd = request.getEndDateTime() != null ? request.getEndDateTime() : b.getEndDateTime();
        if (!newEnd.isAfter(newStart)) {
            throw new BusinessException("Kraj termina mora biti posle pocetka");
        }
        boolean timesChanged = request.getStartDateTime() != null || request.getEndDateTime() != null;
        if (timesChanged) {
            boolean conflict = bookingRepository.findOverlapping(b.getRoomId(), newStart, newEnd, ACTIVE_STATUSES)
                    .stream().anyMatch(other -> !other.getId().equals(id));
            if (conflict) {
                throw new BusinessException("Novi termin se preklapa sa postojecom rezervacijom");
            }
        }

        b.setStartDateTime(newStart);
        b.setEndDateTime(newEnd);
        if (request.getPurpose() != null) b.setPurpose(request.getPurpose());
        if (request.getTeachingType() != null) b.setTeachingType(request.getTeachingType());
        if (request.getNotes() != null) b.setNotes(request.getNotes());

        return toDTO(bookingRepository.save(b));
    }

    @Override
    @Transactional
    public BookingDTO approve(Long id, Long approvedBy) {
        Booking b = requireRequested(id);
        b.setStatus(BookingStatus.ACCEPTED);
        b.setApprovedBy(approvedBy);
        return toDTO(bookingRepository.save(b));
    }

    @Override
    @Transactional
    public BookingDTO reject(Long id, Long approvedBy) {
        Booking b = requireRequested(id);
        b.setStatus(BookingStatus.REJECTED);
        b.setApprovedBy(approvedBy);
        return toDTO(bookingRepository.save(b));
    }

    @Override
    @Transactional
    public BookingDTO cancel(Long id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REJECTED) {
            throw new BusinessException("Rezervacija je vec zatvorena (" + b.getStatus() + ")");
        }
        b.setStatus(BookingStatus.CANCELLED);
        return toDTO(bookingRepository.save(b));
    }

    private Booking requireRequested(Long id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        if (b.getStatus() != BookingStatus.REQUESTED) {
            throw new BusinessException("Moze se obraditi samo rezervacija na cekanju (trenutno: " + b.getStatus() + ")");
        }
        return b;
    }

    private BookingDTO toDTO(Booking b) {
        String roomName = roomRepository.findById(b.getRoomId()).map(Room::getName).orElse(null);
        return bookingMapper.toDTO(b, roomName);
    }

    /** Batch mapiranje: jedan upit za sva imena prostorija umesto po jedan za svaki booking. */
    private List<BookingDTO> toDTOs(List<Booking> bookings) {
        Map<Long, String> roomNames = roomRepository.findAllById(
                        bookings.stream().map(Booking::getRoomId).filter(rid -> rid != null).distinct().toList())
                .stream().collect(Collectors.toMap(Room::getId, Room::getName));
        return bookings.stream()
                .map(b -> bookingMapper.toDTO(b, roomNames.get(b.getRoomId())))
                .toList();
    }
}
