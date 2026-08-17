package com.aradosavljevic.schedule_service.application.request.room;

import lombok.Data;

@Data
public class RoomUpdateRequest {
    private String name;
    private String building;
    private Integer floor;
    private String roomNumber;
    private Integer capacity;
    private String roomType;
    private Integer computerCount;
    private Boolean bookable;
    private Boolean active;
}
