package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.Reservation;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.raf.sk.api.IRasporedFactory;
import rs.raf.sk.api.Prostorija;
import rs.raf.sk.api.Raspored;
import rs.raf.sk.api.Termin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, IRasporedFactory {

    Optional<Reservation> findReservationById(Long id, Pageable pageable);

    Page<Reservation> findReservationByWorkerId(Long workerId, Pageable pageable);

    Page<Reservation> findReservationByWorkerIdAndTimeSlotId(Long workerId, Long timeSlotId, Pageable pageable);

    Page<Reservation> findReservationByStatus(BookingStatus status, Pageable pageable);

    @Override
    public Reservation create() {
        return new RasporedPredavanja();
    }

    @Override
    public Reservation create(List<Prostorija> prostorije, List<Termin> termini) {
        return new RasporedPredavanja(prostorije,termini);
    }

    @Override
    public Reservation create(List<Prostorija> prostorije, List<Termin> termini, LocalDate datumOd, LocalDate datumDo) {
        return new RasporedPredavanja(prostorije,termini,datumOd,datumDo);
    }
}
