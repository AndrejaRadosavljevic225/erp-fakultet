package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.schedule_service.application.dto.RoomDTO;
import com.aradosavljevic.schedule_service.application.request.room.RoomCreateRequest;
import com.aradosavljevic.schedule_service.application.request.room.RoomUpdateRequest;
import org.springframework.data.domain.Pageable;

public interface RoomService {

    PageResponse<RoomDTO> getAll(Pageable pageable);

    RoomDTO getById(Long id);

    RoomDTO create(RoomCreateRequest request);

    RoomDTO update(Long id, RoomUpdateRequest request);

    void delete(Long id);
}
