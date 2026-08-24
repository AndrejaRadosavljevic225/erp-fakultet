package com.aradosavljevic.schedule_service.infrastructure;

import com.aradosavljevic.schedule_service.domain.entity.Booking;
import com.aradosavljevic.schedule_service.domain.entity.Room;
import com.aradosavljevic.schedule_service.domain.entity.SchoolYear;
import com.aradosavljevic.schedule_service.domain.entity.SchoolYearWorker;
import com.aradosavljevic.schedule_service.domain.entity.TeachingNorm;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import com.aradosavljevic.schedule_service.domain.enums.TeachingType;
import com.aradosavljevic.schedule_service.domain.repository.BookingRepository;
import com.aradosavljevic.schedule_service.domain.repository.RoomRepository;
import com.aradosavljevic.schedule_service.domain.repository.SchoolYearRepository;
import com.aradosavljevic.schedule_service.domain.repository.SchoolYearWorkerRepository;
import com.aradosavljevic.schedule_service.domain.repository.TeachingNormRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Puni praznu bazu rasporeda: prostorije, tekuca skolska godina, norma casova,
 * dodele nastavnika i nekoliko termina u tekucoj nedelji (odobrenih i onih koji
 * cekaju odobrenje), da kalendar, ekran za odobravanje i fond casova ne budu prazni.
 *
 * Pokrece se SAMO ako nema nijedne prostorije. Iskljucuje se sa `app.demo-data=false`.
 *
 * NAPOMENA: workerId i roleId su brojevi iz hr-service-a (nema strane veze medju bazama).
 * Vrednosti ispod odgovaraju redosledu kojim hr-service puni praznu bazu
 * (role ADMIN=1, HR=2, PROFESOR=3; zaposleni Petar=1, Ana=2, Marko=3, Jelena=4).
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private final RoomRepository roomRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final TeachingNormRepository teachingNormRepository;
    private final SchoolYearWorkerRepository schoolYearWorkerRepository;
    private final BookingRepository bookingRepository;

    @Value("${app.demo.professor-role-id:3}")
    private Long profesorRoleId;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (roomRepository.count() > 0) {
            return;
        }
        log.info("Prazna baza — upisujem demonstracione podatke rasporeda");

        Room amfiteatar = room("RAF-101", "Amfiteatar 1", "Glavna zgrada", 1, 120, "AMFITEATAR", null);
        Room ucionica = room("RAF-201", "Ucionica 201", "Glavna zgrada", 2, 40, "UCIONICA", null);
        Room lab = room("LAB-01", "Racunarska laboratorija", "Glavna zgrada", 0, 24, "LABORATORIJA", 24);
        room("KAB-12", "Kabinet 12", "Aneks", 1, 12, "KABINET", null);

        SchoolYear year = currentSchoolYear();
        TeachingNorm norm = norm(year, 12, "Norma casova za nastavno osoblje");

        assign(year, 1L, norm);   // Petar Petrovic
        assign(year, 3L, norm);   // Marko Markovic

        // Termini tekuce nedelje, da kalendar i izvestaji odmah imaju sadrzaj.
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        booking(amfiteatar, 1L, year, monday.atTime(8, 0), monday.atTime(10, 0),
                "Predavanje: Baze podataka", TeachingType.REGULAR, BookingStatus.ACCEPTED);
        booking(ucionica, 3L, year, monday.plusDays(1).atTime(10, 0), monday.plusDays(1).atTime(12, 0),
                "Vezbe: Programiranje", TeachingType.REGULAR, BookingStatus.ACCEPTED);
        booking(amfiteatar, 1L, year, monday.plusDays(2).atTime(12, 0), monday.plusDays(2).atTime(14, 0),
                "Dodatna nastava", TeachingType.EXTRA, BookingStatus.ACCEPTED);
        booking(lab, 4L, year, monday.plusDays(3).atTime(9, 0), monday.plusDays(3).atTime(11, 0),
                "Radionica za studente", null, BookingStatus.REQUESTED);
        booking(ucionica, 1L, year, monday.plusDays(4).atTime(14, 0), monday.plusDays(4).atTime(16, 0),
                "Konsultacije", TeachingType.MENTORSHIP, BookingStatus.REQUESTED);

        log.info("Upisano: {} prostorija, {} termina, skolska godina {}",
                roomRepository.count(), bookingRepository.count(), year.getCode());
    }

    private Room room(String code, String name, String building, Integer floor, Integer capacity,
                      String type, Integer computers) {
        Room room = new Room();
        room.setCode(code);
        room.setName(name);
        room.setBuilding(building);
        room.setFloor(floor);
        room.setCapacity(capacity);
        room.setRoomType(type);
        room.setComputerCount(computers);
        room.setBookable(true);
        room.setActive(true);
        return roomRepository.save(room);
    }

    /** Skolska godina koja obuhvata danasnji datum (pocinje 1. oktobra). */
    private SchoolYear currentSchoolYear() {
        LocalDate today = LocalDate.now();
        int start = today.getMonthValue() >= 10 ? today.getYear() : today.getYear() - 1;
        SchoolYear year = new SchoolYear();
        year.setCode(start + "/" + String.valueOf(start + 1).substring(2));
        year.setStartDate(LocalDate.of(start, 10, 1));
        year.setEndDate(LocalDate.of(start + 1, 9, 30));
        year.setDescription("Tekuca skolska godina");
        return schoolYearRepository.save(year);
    }

    private TeachingNorm norm(SchoolYear year, int hours, String description) {
        TeachingNorm norm = new TeachingNorm();
        norm.setRoleId(profesorRoleId);
        norm.setSchoolYearId(year.getId());
        norm.setRequiredHours(hours);
        norm.setDescription(description);
        return teachingNormRepository.save(norm);
    }

    private void assign(SchoolYear year, Long workerId, TeachingNorm norm) {
        SchoolYearWorker assignment = new SchoolYearWorker();
        assignment.setSchoolYearId(year.getId());
        assignment.setWorkerId(workerId);
        assignment.setRoleId(profesorRoleId);
        assignment.setNormId(norm.getId());
        schoolYearWorkerRepository.save(assignment);
    }

    private void booking(Room room, Long workerId, SchoolYear year, LocalDateTime start, LocalDateTime end,
                         String purpose, TeachingType type, BookingStatus status) {
        Booking booking = new Booking();
        booking.setRoomId(room.getId());
        booking.setRequesterWorkerId(workerId);
        booking.setSchoolYearId(year.getId());
        booking.setStartDateTime(start);
        booking.setEndDateTime(end);
        booking.setPurpose(purpose);
        booking.setTeachingType(type);
        booking.setStatus(status);
        if (status == BookingStatus.ACCEPTED) {
            booking.setApprovedBy(1L);
        }
        booking.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }
}
