package com.aradosavljevic.schedule_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.AvailabilityResponse;
import com.aradosavljevic.schedule_service.application.dto.BookingDTO;
import com.aradosavljevic.schedule_service.application.request.booking.AvailabilityRequest;
import com.aradosavljevic.schedule_service.application.request.booking.BookingCreateRequest;
import com.aradosavljevic.schedule_service.application.request.booking.BookingUpdateRequest;
import com.aradosavljevic.schedule_service.application.service.BookingService;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingDTO> create(@Valid @RequestBody BookingCreateRequest request) {
        return ApiResponse.success("Rezervacija je podneta (ceka odobrenje)", bookingService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(bookingService.getById(id));
    }

    @GetMapping("/room/{roomId}")
    public ApiResponse<PageResponse<BookingDTO>> byRoom(@PathVariable Long roomId,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(bookingService.getByRoom(roomId, pageable));
    }

    @GetMapping("/worker/{workerId}")
    public ApiResponse<PageResponse<BookingDTO>> byWorker(@PathVariable Long workerId,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(bookingService.getByWorker(workerId, pageable));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<PageResponse<BookingDTO>> byStatus(@PathVariable BookingStatus status,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(bookingService.getByStatus(status, pageable));
    }

    @GetMapping("/room/{roomId}/occupancy")
    public ApiResponse<List<BookingDTO>> occupancy(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.success(bookingService.occupancy(roomId, from, to));
    }

    @PostMapping("/availability")
    public ApiResponse<AvailabilityResponse> availability(@Valid @RequestBody AvailabilityRequest request) {
        return ApiResponse.success(bookingService.checkAvailability(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookingDTO> update(@PathVariable Long id, @Valid @RequestBody BookingUpdateRequest request) {
        return ApiResponse.success("Rezervacija je azurirana", bookingService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<BookingDTO> approve(@PathVariable Long id,
                                           @RequestParam(required = false) Long approvedBy) {
        return ApiResponse.success("Rezervacija je odobrena", bookingService.approve(id, approvedBy));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<BookingDTO> reject(@PathVariable Long id,
                                          @RequestParam(required = false) Long approvedBy) {
        return ApiResponse.success("Rezervacija je odbijena", bookingService.reject(id, approvedBy));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookingDTO> cancel(@PathVariable Long id) {
        return ApiResponse.success("Rezervacija je otkazana", bookingService.cancel(id));
    }
}
