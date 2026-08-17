package com.aradosavljevic.schedule_service.application.mapper;

import com.aradosavljevic.schedule_service.application.dto.RoomDTO;
import com.aradosavljevic.schedule_service.domain.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomDTO toDTO(Room r) {
        if (r == null) return null;
        return RoomDTO.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .building(r.getBuilding())
                .floor(r.getFloor())
                .roomNumber(r.getRoomNumber())
                .capacity(r.getCapacity())
                .roomType(r.getRoomType())
                .computerCount(r.getComputerCount())
                .bookable(Boolean.TRUE.equals(r.getBookable()))
                .active(Boolean.TRUE.equals(r.getActive()))
                .build();
    }
}
