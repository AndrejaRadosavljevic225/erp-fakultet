package com.aradosavljevic.schedule_service.domain.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Setter
@Getter
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long schoolYearId;

    private Long roomId;

    private LocalDate date;
    private LocalTime timeStart;
    private LocalTime timeFinish;

    private  Boolean isAvailable;

//    TODO: Dodatni podaci

}

//Table room_scheduled {
//booking_id uuid [pk]
//room_id uuid
//requester_worker_id uuid
//start_datetime timestamp
//end_datetime timestamp
//purpose varchar
//status varchar
//teaching_type varchar
//approved_by uuid
//notes text
//created_at timestamp
//
//indexes {
//    (room_id, start_datetime, end_datetime) [name: 'idx_room_time_overlap']
//}
//}