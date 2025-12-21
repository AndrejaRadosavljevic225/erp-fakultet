package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;


@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot,Long> {


    Page<TimeSlot> findTimeSlotByRoomId(Long roomId, Pageable pageable);

    Page<TimeSlot> findTimeSlotByRoomIdAndDate(Long roomId, LocalDate date, Pageable pageable);

    Page<TimeSlot> findTimeSlotByRoomIdAndDateAndTimeStartAndTimeFinish(Long roomId, LocalDate date, LocalTime timeStart, LocalTime timeFinish, Pageable pageable);

    Page<TimeSlot> findTimeSlotByDateAndTimeStartAndTimeFinish(LocalDate date, LocalTime timeStart, LocalTime timeFinish, Pageable pageable);

    Page<TimeSlot> findAllByOrderByTimeStart(Pageable pageable);

}
