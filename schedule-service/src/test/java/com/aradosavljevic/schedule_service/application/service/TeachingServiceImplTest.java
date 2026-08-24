package com.aradosavljevic.schedule_service.application.service;

import com.aradosavljevic.erp_common.exception.ResourceNotFoundException;
import com.aradosavljevic.schedule_service.application.dto.TeachingReportDTO;
import com.aradosavljevic.schedule_service.application.mapper.TeachingMapper;
import com.aradosavljevic.schedule_service.domain.entity.Booking;
import com.aradosavljevic.schedule_service.domain.entity.SchoolYearWorker;
import com.aradosavljevic.schedule_service.domain.entity.TeachingNorm;
import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import com.aradosavljevic.schedule_service.domain.enums.TeachingType;
import com.aradosavljevic.schedule_service.domain.repository.BookingRepository;
import com.aradosavljevic.schedule_service.domain.repository.SchoolYearRepository;
import com.aradosavljevic.schedule_service.domain.repository.SchoolYearWorkerRepository;
import com.aradosavljevic.schedule_service.domain.repository.TeachingNormRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Fond casova (UC-HR-03): norma po zvanju naspram realizovanih sati iz odrzanih
 * nastavnih termina. U izracunavanje ulaze SAMO termini koji imaju tip nastave
 * i ciji je status ACCEPTED ili FINISHED.
 */
@ExtendWith(MockitoExtension.class)
class TeachingServiceImplTest {

    private static final Long PROFESOR = 7L;
    private static final Long GODINA = 1L;
    private static final Long NORMA = 5L;

    @Mock
    private TeachingNormRepository teachingNormRepository;
    @Mock
    private SchoolYearWorkerRepository schoolYearWorkerRepository;
    @Mock
    private SchoolYearRepository schoolYearRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TeachingMapper teachingMapper;

    @InjectMocks
    private TeachingServiceImpl teachingService;

    @BeforeEach
    void prijaviAdmina() {
        prijavi("admin", "ROLE_ADMIN");
    }

    @AfterEach
    void odjavi() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Norma 12h i dva odrzana casa po 2h daju odstupanje od -8h")
    void nepopunjenaNorma() {
        dodeljenNastavnik();
        norma(12);
        termini(
                cas(8, 10, TeachingType.REGULAR, BookingStatus.ACCEPTED),
                cas(10, 12, TeachingType.REGULAR, BookingStatus.ACCEPTED));

        TeachingReportDTO izvestaj = teachingService.report(PROFESOR, GODINA);

        assertThat(izvestaj.getRequiredHours()).isEqualTo(12);
        assertThat(izvestaj.getRealizedHours()).isEqualTo(4.0);
        assertThat(izvestaj.getDeviation()).isEqualTo(-8.0);
        assertThat(izvestaj.getExtraHours()).isZero();
        assertThat(izvestaj.isFulfilled()).isFalse();
    }

    @Test
    @DisplayName("Prekoracenje norme se prikazuje kao prekovremeni sati")
    void prekoracenaNorma() {
        dodeljenNastavnik();
        norma(4);
        termini(
                cas(8, 12, TeachingType.REGULAR, BookingStatus.ACCEPTED),
                cas(12, 14, TeachingType.EXTRA, BookingStatus.FINISHED));

        TeachingReportDTO izvestaj = teachingService.report(PROFESOR, GODINA);

        assertThat(izvestaj.getRealizedHours()).isEqualTo(6.0);
        assertThat(izvestaj.getDeviation()).isEqualTo(2.0);
        assertThat(izvestaj.getExtraHours()).isEqualTo(2.0);
        assertThat(izvestaj.isFulfilled()).isTrue();
    }

    @Test
    @DisplayName("Nenastavna rezervacija (bez tipa nastave) ne ulazi u fond casova")
    void nenastavniTerminSeNeBroji() {
        dodeljenNastavnik();
        norma(10);
        termini(
                cas(8, 10, TeachingType.REGULAR, BookingStatus.ACCEPTED),
                cas(10, 14, null, BookingStatus.ACCEPTED));

        assertThat(teachingService.report(PROFESOR, GODINA).getRealizedHours()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Termini koji cekaju odobrenje, odbijeni i otkazani se ne broje")
    void neodobreniTerminiSeNeBroje() {
        dodeljenNastavnik();
        norma(10);
        termini(
                cas(8, 10, TeachingType.REGULAR, BookingStatus.ACCEPTED),
                cas(10, 12, TeachingType.REGULAR, BookingStatus.REQUESTED),
                cas(12, 14, TeachingType.REGULAR, BookingStatus.REJECTED),
                cas(14, 16, TeachingType.REGULAR, BookingStatus.CANCELLED));

        assertThat(teachingService.report(PROFESOR, GODINA).getRealizedHours()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Kad nastavnik nema normu, kvota je 0 pa je norma ispunjena")
    void bezNorme() {
        SchoolYearWorker dodela = new SchoolYearWorker();
        dodela.setWorkerId(PROFESOR);
        dodela.setSchoolYearId(GODINA);
        when(schoolYearWorkerRepository.findByWorkerIdAndSchoolYearId(PROFESOR, GODINA))
                .thenReturn(Optional.of(dodela));
        termini();

        TeachingReportDTO izvestaj = teachingService.report(PROFESOR, GODINA);

        assertThat(izvestaj.getRequiredHours()).isZero();
        assertThat(izvestaj.isFulfilled()).isTrue();
    }

    @Test
    @DisplayName("Nastavnik koji nije dodeljen godini nema izvestaj")
    void nedodeljenNastavnik() {
        when(schoolYearWorkerRepository.findByWorkerIdAndSchoolYearId(PROFESOR, GODINA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> teachingService.report(PROFESOR, GODINA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Profesor ne moze da vidi tudji fond casova")
    void profesorNeVidiTudjiFond() {
        prijavi("profesor", "ROLE_PROFESOR");

        assertThatThrownBy(() -> teachingService.report(PROFESOR, GODINA))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --- pomocne metode ---

    private void dodeljenNastavnik() {
        SchoolYearWorker dodela = new SchoolYearWorker();
        dodela.setWorkerId(PROFESOR);
        dodela.setSchoolYearId(GODINA);
        dodela.setNormId(NORMA);
        when(schoolYearWorkerRepository.findByWorkerIdAndSchoolYearId(PROFESOR, GODINA))
                .thenReturn(Optional.of(dodela));
    }

    private void norma(int sati) {
        TeachingNorm norm = new TeachingNorm();
        norm.setId(NORMA);
        norm.setSchoolYearId(GODINA);
        norm.setRequiredHours(sati);
        when(teachingNormRepository.findById(NORMA)).thenReturn(Optional.of(norm));
    }

    private void termini(Booking... termini) {
        when(bookingRepository.findByRequesterWorkerIdAndSchoolYearId(PROFESOR, GODINA))
                .thenReturn(List.of(termini));
    }

    private static Booking cas(int od, int doSat, TeachingType tip, BookingStatus status) {
        Booking booking = new Booking();
        booking.setRequesterWorkerId(PROFESOR);
        booking.setSchoolYearId(GODINA);
        booking.setStartDateTime(LocalDateTime.of(2027, 3, 1, od, 0));
        booking.setEndDateTime(LocalDateTime.of(2027, 3, 1, doSat, 0));
        booking.setTeachingType(tip);
        booking.setStatus(status);
        return booking;
    }

    /** Profesor koji trazi tudji izvestaj nema workerId koji se poklapa sa trazenim. */
    private void prijavi(String korisnik, String rola) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(korisnik, null,
                        List.of(new SimpleGrantedAuthority(rola))));
    }
}
