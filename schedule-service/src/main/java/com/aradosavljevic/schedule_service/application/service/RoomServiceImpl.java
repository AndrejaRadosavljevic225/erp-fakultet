package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.dto.PageResponse;
import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.schedule_service.application.dto.RoomDTO;
import com.aradosavljevic.schedule_service.application.mapper.PageMapper;
import com.aradosavljevic.schedule_service.application.mapper.RoomMapper;
import com.aradosavljevic.schedule_service.application.request.room.RoomCreateRequest;
import com.aradosavljevic.schedule_service.application.request.room.RoomUpdateRequest;
import com.aradosavljevic.schedule_service.domain.entity.Room;
import com.aradosavljevic.schedule_service.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomDTO> getAll(Pageable pageable) {
        return PageMapper.toPageResponse(roomRepository.findAll(pageable), roomMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDTO getById(Long id) {
        return roomRepository.findById(id)
                .map(roomMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
    }

    @Override
    @Transactional
    public RoomDTO create(RoomCreateRequest request) {
        Room room = new Room();
        room.setCode(request.getCode());
        room.setName(request.getName());
        room.setBuilding(request.getBuilding());
        room.setFloor(request.getFloor());
        room.setRoomNumber(request.getRoomNumber());
        room.setCapacity(request.getCapacity());
        room.setRoomType(request.getRoomType());
        room.setComputerCount(request.getComputerCount());
        room.setBookable(request.getBookable() != null ? request.getBookable() : true);
        room.setActive(true);
        return roomMapper.toDTO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomDTO update(Long id, RoomUpdateRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        if (request.getName() != null) room.setName(request.getName());
        if (request.getBuilding() != null) room.setBuilding(request.getBuilding());
        if (request.getFloor() != null) room.setFloor(request.getFloor());
        if (request.getRoomNumber() != null) room.setRoomNumber(request.getRoomNumber());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());
        if (request.getComputerCount() != null) room.setComputerCount(request.getComputerCount());
        if (request.getBookable() != null) room.setBookable(request.getBookable());
        if (request.getActive() != null) room.setActive(request.getActive());

        return roomMapper.toDTO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room", "id", id);
        }
        roomRepository.deleteById(id);
    }
}
