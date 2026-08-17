package com.aradosavljevic.schedule_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private String code;
    private String name;
    private String building;
    private Integer floor;
    private String roomNumber;
    private Integer capacity;
    private String roomType;
    private Integer computerCount;
    private boolean bookable;
    private boolean active;
}
