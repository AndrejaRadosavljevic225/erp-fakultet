package com.aradosavljevic.schedule_service.domain.entity;


import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timeSlotId;
    private Long workerId;

    private String description;

    private boolean repeating;
    private Long number;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reservation", orphanRemoval = true)
    private List<Tag> tags=new ArrayList<>();

    private BookingStatus status = BookingStatus.REQUESTED;

}
