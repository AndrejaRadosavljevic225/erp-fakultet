package com.aradosavljevic.hr_service.api.controller;

import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorizacija po rolama nad zaposlenima. Poenta ovih testova je da pravila
 * vaze na SERVERU (@PreAuthorize), nezavisno od toga sta korisnicki interfejs
 * prikazuje ili sakriva.
 *
 * Ocekivano: ADMIN sve, HR sve osim brisanja, PROFESOR samo citanje.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WorkerControllerSecurityTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final AtomicInteger BROJAC = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String hrToken;
    private String profesorToken;
    private Long postojeciZaposleniId;

    @BeforeEach
    void pripremiNaloge() throws Exception {
        nalog("hr-test", "HR");
        nalog("prof-test", "PROFESOR");

        adminToken = token("admin", "admin123");
        hrToken = token("hr-test", "test1234");
        profesorToken = token("prof-test", "test1234");

        postojeciZaposleniId = workerRepository.save(zaposleni()).getId();
    }

    // --- citanje ---

    @Test
    @DisplayName("Sve tri role mogu da citaju listu zaposlenih")
    void citanjeDozvoljenoSvima() throws Exception {
        for (String token : new String[] {adminToken, hrToken, profesorToken}) {
            mockMvc.perform(get("/api/workers").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Bez tokena nema pristupa")
    void bezTokenaNemaPristupa() throws Exception {
        mockMvc.perform(get("/api/workers"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Nevalidan token se odbija")
    void nevalidanToken() throws Exception {
        mockMvc.perform(get("/api/workers").header("Authorization", "Bearer ovo.nije.token"))
                .andExpect(status().is4xxClientError());
    }

    // --- pisanje ---

    @Test
    @DisplayName("ADMIN moze da kreira zaposlenog")
    void adminKreira() throws Exception {
        mockMvc.perform(kreiranje(adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR moze da kreira zaposlenog")
    void hrKreira() throws Exception {
        mockMvc.perform(kreiranje(hrToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PROFESOR ne moze da kreira zaposlenog")
    void profesorNeKreira() throws Exception {
        mockMvc.perform(kreiranje(profesorToken))
                .andExpect(status().isForbidden());
    }

    // --- brisanje ---

    @Test
    @DisplayName("Brisanje zaposlenog je rezervisano za ADMIN-a")
    void adminBrise() throws Exception {
        mockMvc.perform(delete("/api/workers/" + postojeciZaposleniId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HR ne moze da brise zaposlenog")
    void hrNeBrise() throws Exception {
        mockMvc.perform(delete("/api/workers/" + postojeciZaposleniId)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROFESOR ne moze da brise zaposlenog")
    void profesorNeBrise() throws Exception {
        mockMvc.perform(delete("/api/workers/" + postojeciZaposleniId)
                        .header("Authorization", "Bearer " + profesorToken))
                .andExpect(status().isForbidden());
    }

    // --- RBAC nad administrativnim resursima ---

    @Test
    @DisplayName("PROFESOR ne vidi korisnicke naloge ni istoriju izmena")
    void profesorNeViDiAdministraciju() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + profesorToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/audit-logs").header("Authorization", "Bearer " + profesorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HR cita role, ali ne moze da ih kreira")
    void hrCitaAliNeKreiraRole() throws Exception {
        mockMvc.perform(get("/api/roles").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"NOVA\",\"name\":\"Nova rola\"}"))
                .andExpect(status().isForbidden());
    }

    // --- pomocne metode ---

    private void nalog(String korisnickoIme, String kodRole) {
        if (userAccountRepository.findByUsername(korisnickoIme).isPresent()) {
            return;
        }
        Long roleId = roleRepository.findByCode(kodRole).map(Role::getId).orElseThrow();
        UserAccount nalog = new UserAccount();
        nalog.setUsername(korisnickoIme);
        nalog.setPasswordHash(passwordEncoder.encode("test1234"));
        nalog.setRoleId(roleId);
        nalog.setIsActive(true);
        nalog.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(nalog);
    }

    private Worker zaposleni() {
        int broj = BROJAC.incrementAndGet();
        Worker worker = new Worker();
        worker.setFirstName("Test");
        worker.setLastName("Zaposleni" + broj);
        worker.setEmail("test" + broj + "@fakultet.rs");
        worker.setPersonalId(String.format("%013d", 1000000000000L + broj));
        worker.setHireDate(LocalDate.of(2024, 1, 1));
        worker.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return worker;
    }

    private MockHttpServletRequestBuilder kreiranje(String token) {
        int broj = BROJAC.incrementAndGet();
        return post("/api/workers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"Novi","lastName":"Zaposleni%d","email":"novi%d@fakultet.rs",
                         "personalId":"%013d","hireDate":"2026-01-01"}
                        """.formatted(broj, broj, 2000000000000L + broj));
    }

    private String token(String korisnik, String lozinka) throws Exception {
        String odgovor = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\":\"" + korisnik + "\",\"password\":\"" + lozinka + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = TOKEN.matcher(odgovor);
        if (!matcher.find()) {
            throw new IllegalStateException("Prijava nije vratila token: " + odgovor);
        }
        return matcher.group(1);
    }
}
