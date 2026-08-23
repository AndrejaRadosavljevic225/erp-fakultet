package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.Booking;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByRoomId(Long roomId, Pageable pageable);

    Page<Booking> findByRequesterWorkerId(Long requesterWorkerId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    List<Booking> findByRoomIdAndStartDateTimeBetween(Long roomId, LocalDateTime from, LocalDateTime to);

    List<Booking> findByRequesterWorkerIdAndSchoolYearId(Long requesterWorkerId, Long schoolYearId);

    /**
     * Postoji li aktivna rezervacija (u zadatim statusima) koja se preklapa sa [start, end)
     * u istoj prostoriji. Preklapanje: postojeci.start < novi.end AND postojeci.end > novi.start.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.roomId = :roomId AND b.status IN :statuses " +
            "AND b.startDateTime < :end AND b.endDateTime > :start")
    boolean existsOverlap(@Param("roomId") Long roomId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end,
                          @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.roomId = :roomId AND b.status IN :statuses " +
            "AND b.startDateTime < :end AND b.endDateTime > :start")
    List<Booking> findOverlapping(@Param("roomId") Long roomId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("statuses") List<BookingStatus> statuses);

    /**
     * Sve rezervacije u zadatim prostorijama koje se preklapaju sa intervalom [start, end).
     * Koristi se za kalendar zauzetosti vise sala odjednom (UC-SC-05).
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.roomId IN :roomIds AND b.status IN :statuses " +
            "AND b.startDateTime < :end AND b.endDateTime > :start " +
            "ORDER BY b.startDateTime")
    List<Booking> findOverlappingInRooms(@Param("roomIds") List<Long> roomIds,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("statuses") List<BookingStatus> statuses);
}
