package com.aradosavljevic.schedule_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "room", indexes = {
        @Index(name = "idx_room_code", columnList = "code", unique = true)
})
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String name;

    private String building;

    private Integer floor;

    private String roomNumber;

    private Integer capacity;

    private String roomType;

    private Integer computerCount;

    private Boolean bookable = true;

    private Boolean active = true;
}
