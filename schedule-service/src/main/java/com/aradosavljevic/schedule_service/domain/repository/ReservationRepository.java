package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.Reservation;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    Optional<Reservation> findReservationById(Long id, Pageable pageable);

    Page<Reservation> findReservationByWorkerId(Long workerId, Pageable pageable);

    Page<Reservation> findReservationByWorkerIdAndTimeSlotId(Long workerId, Long timeSlotId, Pageable pageable);

    Page<Reservation> findReservationByStatus(BookingStatus status, Pageable pageable);

}
