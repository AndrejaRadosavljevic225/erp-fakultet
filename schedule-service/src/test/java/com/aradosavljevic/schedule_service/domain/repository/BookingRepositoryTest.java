package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.Booking;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import com.aradosavljevic.schedule_service.domain.enums.TeachingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Provera upita za preklapanje termina — srca rezervacije prostorija (UC-SC-02).
 * Pravilo: termini se preklapaju ako je postojeci.start < novi.end I postojeci.end > novi.start,
 * pa dodirivanje krajeva (10-12 i 12-14) NIJE preklapanje.
 */
@DataJpaTest
class BookingRepositoryTest {

    private static final Long SALA = 1L;
    private static final Long DRUGA_SALA = 2L;
    private static final List<BookingStatus> AKTIVNI =
            List.of(BookingStatus.REQUESTED, BookingStatus.ACCEPTED);

    @Autowired
    private BookingRepository bookingRepository;

    /** Postojeci termin: 1. mart 2027, 10:00–12:00, sala 1, odobren. */
    private static final LocalDateTime POSTOJECI_OD = LocalDateTime.of(2027, 3, 1, 10, 0);
    private static final LocalDateTime POSTOJECI_DO = LocalDateTime.of(2027, 3, 1, 12, 0);

    @BeforeEach
    void pripremiPostojeciTermin() {
        bookingRepository.deleteAll();
        bookingRepository.save(termin(SALA, POSTOJECI_OD, POSTOJECI_DO, BookingStatus.ACCEPTED));
    }

    @Test
    @DisplayName("Termin koji pocinje unutar postojeceg se preklapa")
    void preklapanjeNaPocetku() {
        assertThat(postojiPreklapanje(SALA, sat(11), sat(13))).isTrue();
    }

    @Test
    @DisplayName("Termin koji se zavrsava unutar postojeceg se preklapa")
    void preklapanjeNaKraju() {
        assertThat(postojiPreklapanje(SALA, sat(9), sat(11))).isTrue();
    }

    @Test
    @DisplayName("Termin u potpunosti unutar postojeceg se preklapa")
    void terminUnutar() {
        assertThat(postojiPreklapanje(SALA, sat(10, 30), sat(11, 30))).isTrue();
    }

    @Test
    @DisplayName("Termin koji obuhvata postojeci se preklapa")
    void terminObuhvata() {
        assertThat(postojiPreklapanje(SALA, sat(9), sat(13))).isTrue();
    }

    @Test
    @DisplayName("Termin koji pocinje tacno kad prethodni zavrsava NIJE preklapanje")
    void dodirivanjeKrajeva() {
        assertThat(postojiPreklapanje(SALA, sat(12), sat(14))).isFalse();
    }

    @Test
    @DisplayName("Termin koji se zavrsava tacno kad sledeci pocinje NIJE preklapanje")
    void dodirivanjePocetka() {
        assertThat(postojiPreklapanje(SALA, sat(8), sat(10))).isFalse();
    }

    @Test
    @DisplayName("Isti termin u drugoj sali nije preklapanje")
    void drugaSala() {
        assertThat(postojiPreklapanje(DRUGA_SALA, sat(10), sat(12))).isFalse();
    }

    @Test
    @DisplayName("Otkazan termin ne blokira novi")
    void otkazanTerminNeBlokira() {
        bookingRepository.deleteAll();
        bookingRepository.save(termin(SALA, POSTOJECI_OD, POSTOJECI_DO, BookingStatus.CANCELLED));

        assertThat(postojiPreklapanje(SALA, sat(10), sat(12))).isFalse();
    }

    @Test
    @DisplayName("findOverlapping vraca sam konfliktni termin, da korisnik vidi sa cim se sudara")
    void vracaKonflikte() {
        List<Booking> konflikti =
                bookingRepository.findOverlapping(SALA, sat(11), sat(13), AKTIVNI);

        assertThat(konflikti).hasSize(1);
        assertThat(konflikti.get(0).getStartDateTime()).isEqualTo(POSTOJECI_OD);
    }

    @Test
    @DisplayName("Zauzetost hvata i termin koji je poceo PRE trazenog intervala")
    void zauzetostHvataTerminUToku() {
        // Interval 11:00-11:30 je u celosti unutar postojeceg termina 10:00-12:00.
        List<Booking> uToku =
                bookingRepository.findOverlapping(SALA, sat(11), sat(11, 30), AKTIVNI);

        assertThat(uToku).hasSize(1);
    }

    @Test
    @DisplayName("Zauzetost vise sala vraca termine iz svih trazenih prostorija")
    void zauzetostViseSala() {
        bookingRepository.save(termin(DRUGA_SALA, sat(14), sat(16), BookingStatus.REQUESTED));

        List<Booking> svi = bookingRepository.findOverlappingInRooms(
                List.of(SALA, DRUGA_SALA), sat(8), sat(20), AKTIVNI);

        assertThat(svi).hasSize(2);
        assertThat(svi).extracting(Booking::getRoomId).containsExactlyInAnyOrder(SALA, DRUGA_SALA);
    }

    @Test
    @DisplayName("Zauzetost je sortirana po pocetku termina")
    void zauzetostSortirana() {
        bookingRepository.save(termin(SALA, sat(8), sat(9), BookingStatus.ACCEPTED));

        List<Booking> svi = bookingRepository.findOverlappingInRooms(
                List.of(SALA), sat(0), sat(23), AKTIVNI);

        assertThat(svi).extracting(Booking::getStartDateTime).isSorted();
    }

    private boolean postojiPreklapanje(Long salaId, LocalDateTime od, LocalDateTime doVremena) {
        return bookingRepository.existsOverlap(salaId, od, doVremena, AKTIVNI);
    }

    private static LocalDateTime sat(int sat) {
        return LocalDateTime.of(2027, 3, 1, sat, 0);
    }

    private static LocalDateTime sat(int sat, int minut) {
        return LocalDateTime.of(2027, 3, 1, sat, minut);
    }

    private static Booking termin(Long salaId, LocalDateTime od, LocalDateTime doVremena, BookingStatus status) {
        Booking booking = new Booking();
        booking.setRoomId(salaId);
        booking.setRequesterWorkerId(1L);
        booking.setStartDateTime(od);
        booking.setEndDateTime(doVremena);
        booking.setStatus(status);
        booking.setTeachingType(TeachingType.REGULAR);
        booking.setCreatedAt(LocalDateTime.now());
        return booking;
    }
}
