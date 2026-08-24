package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prijava i podaci o prijavljenom korisniku (UC-G-01).
 * Nalog 'admin' i difoltne role pravi DefaultRoleSeeder pri podizanju konteksta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    private static final String EMAIL_ZAPOSLENOG = "petar.petrovic@fakultet.rs";
    private static final String LOZINKA = "prof1234";
    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void pripremiNalogVezanZaZaposlenog() {
        if (userAccountRepository.findByUsername("petar").isPresent()) {
            return;
        }
        Worker worker = new Worker();
        worker.setFirstName("Petar");
        worker.setLastName("Petrovic");
        worker.setEmail(EMAIL_ZAPOSLENOG);
        worker.setPersonalId("0101980710011");
        worker.setHireDate(LocalDate.of(2020, 9, 1));
        worker.setEmploymentStatus(EmploymentStatus.ACTIVE);
        Worker sacuvan = workerRepository.save(worker);

        UserAccount nalog = new UserAccount();
        nalog.setUsername("petar");
        nalog.setPasswordHash(passwordEncoder.encode(LOZINKA));
        nalog.setWorkerId(sacuvan.getId());
        nalog.setIsActive(true);
        nalog.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(nalog);
    }

    @Test
    @DisplayName("Prijava korisnickim imenom vraca JWT")
    void prijavaKorisnickimImenom() throws Exception {
        mockMvc.perform(prijava("admin", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("Prijava email adresom zaposlenog takodje radi (UC-G-01)")
    void prijavaEmailom() throws Exception {
        mockMvc.perform(prijava(EMAIL_ZAPOSLENOG, LOZINKA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("petar"));
    }

    @Test
    @DisplayName("Stari kljuc 'username' u telu zahteva i dalje radi")
    void prijavaStarimKljucem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Pogresna lozinka se odbija")
    void pogresnaLozinka() throws Exception {
        mockMvc.perform(prijava("admin", "pogresno"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Nepostojeci korisnik dobija istu poruku kao pogresna lozinka")
    void nepostojeciKorisnik() throws Exception {
        mockMvc.perform(prijava("neko-koga-nema", "bilosta"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Neispravno korisnicko ime ili lozinka"));
    }

    @Test
    @DisplayName("Prazno telo zahteva pada na validaciji")
    void praznoTelo() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("/auth/me bez tokena nije dostupan")
    void meBezTokena() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("/auth/me vraca rolu i povezanog zaposlenog")
    void mePodaci() throws Exception {
        String token = tokenZa("petar", LOZINKA);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("petar"))
                .andExpect(jsonPath("$.data.workerEmail").value(EMAIL_ZAPOSLENOG))
                .andExpect(jsonPath("$.data.workerFullName").value("Petar Petrovic"));
    }

    @Test
    @DisplayName("Administrator ima ADMIN rolu i listu permisija")
    void meAdministrator() throws Exception {
        String token = tokenZa("admin", "admin123");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions").isArray());
    }

    private MockHttpServletRequestBuilder prijava(String korisnik, String lozinka) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usernameOrEmail\":\"" + korisnik + "\",\"password\":\"" + lozinka + "\"}");
    }

    private String tokenZa(String korisnik, String lozinka) throws Exception {
        String odgovor = mockMvc.perform(prijava(korisnik, lozinka))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = TOKEN.matcher(odgovor);
        if (!matcher.find()) {
            throw new IllegalStateException("Odgovor ne sadrzi token: " + odgovor);
        }
        return matcher.group(1);
    }
}
