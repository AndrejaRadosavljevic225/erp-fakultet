package com.aradosavljevic.schedule_service.api.controller;

import com.aradosavljevic.erp_common.dto.ApiResponse;
import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.RoomDTO;
import com.aradosavljevic.schedule_service.application.request.room.RoomCreateRequest;
import com.aradosavljevic.schedule_service.application.request.room.RoomUpdateRequest;
import com.aradosavljevic.schedule_service.application.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ApiResponse<PageResponse<RoomDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(roomService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(roomService.getById(id));
    }

    @PostMapping
    public ApiResponse<RoomDTO> create(@Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.success("Prostorija je kreirana", roomService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomDTO> update(@PathVariable Long id, @Valid @RequestBody RoomUpdateRequest request) {
        return ApiResponse.success("Prostorija je azurirana", roomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ApiResponse.success("Prostorija je obrisana", null);
    }
}
