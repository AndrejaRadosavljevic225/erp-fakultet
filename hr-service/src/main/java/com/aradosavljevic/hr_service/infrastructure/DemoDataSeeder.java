package com.aradosavljevic.hr_service.infrastructure;

import com.aradosavljevic.hr_service.domain.entity.Permission;
import com.aradosavljevic.hr_service.domain.entity.Position;
import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.entity.RolePermission;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.entity.Worker;
import com.aradosavljevic.hr_service.domain.entity.WorkerPosition;
import com.aradosavljevic.hr_service.domain.enums.EmploymentStatus;
import com.aradosavljevic.hr_service.domain.enums.EmploymentType;
import com.aradosavljevic.hr_service.domain.repository.PermissionRepository;
import com.aradosavljevic.hr_service.domain.repository.PositionRepository;
import com.aradosavljevic.hr_service.domain.repository.RolePermissionRepository;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerPositionRepository;
import com.aradosavljevic.hr_service.domain.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Puni praznu bazu demonstracionim podacima (zaposleni, radna mesta, nalozi, permisije),
 * da bi sistem odmah po prvom pokretanju bio upotrebljiv i pregledan.
 *
 * Pokrece se SAMO ako u bazi nema nijednog zaposlenog, pa ponovno pokretanje ne duplira
 * podatke. Iskljucuje se sa `app.demo-data=false` (tako je podeseno u prod profilu).
 */
@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private final WorkerRepository workerRepository;
    private final PositionRepository positionRepository;
    private final WorkerPositionRepository workerPositionRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (workerRepository.count() > 0) {
            return;
        }
        log.info("Prazna baza — upisujem demonstracione podatke");

        Position redovniProfesor = position("Redovni profesor", "A1", 180000);
        Position docent = position("Docent", "A3", 140000);
        Position asistent = position("Asistent", "B1", 95000);
        Position referent = position("Referent studentske sluzbe", "C2", 80000);

        Worker petar = worker("Petar", "Petrovic", "petar.petrovic@fakultet.rs", "0101980710011",
                "0641110001", LocalDate.of(2015, 9, 1));
        Worker ana = worker("Ana", "Anic", "ana.anic@fakultet.rs", "1505985715022",
                "0641110002", LocalDate.of(2018, 3, 15));
        Worker marko = worker("Marko", "Markovic", "marko.markovic@fakultet.rs", "2203988710033",
                "0641110003", LocalDate.of(2019, 10, 1));
        Worker jelena = worker("Jelena", "Jelic", "jelena.jelic@fakultet.rs", "0712992715044",
                "0641110004", LocalDate.of(2022, 2, 1));
        Worker nikola = worker("Nikola", "Nikolic", "nikola.nikolic@fakultet.rs", "3008990710055",
                "0641110005", LocalDate.of(2021, 6, 1));

        assign(petar, redovniProfesor, LocalDate.of(2015, 9, 1));
        assign(ana, referent, LocalDate.of(2018, 3, 15));
        assign(marko, docent, LocalDate.of(2019, 10, 1));
        assign(jelena, asistent, LocalDate.of(2022, 2, 1));
        assign(nikola, referent, LocalDate.of(2021, 6, 1));

        Long hrRoleId = roleId("HR");
        Long profesorRoleId = roleId("PROFESOR");
        user("hr", "hr1234", ana.getId(), hrRoleId);
        user("profesor", "prof1234", petar.getId(), profesorRoleId);

        Permission workerRead = permission("WORKER_READ", "Pregled zaposlenih", "HR");
        Permission workerWrite = permission("WORKER_WRITE", "Izmena zaposlenih", "HR");
        Permission bookingApprove = permission("BOOKING_APPROVE", "Odobravanje rezervacija", "SCHEDULE");
        Permission teachingReport = permission("TEACHING_REPORT", "Pregled fonda casova", "SCHEDULE");

        grant(hrRoleId, workerRead, workerWrite, bookingApprove, teachingReport);
        grant(profesorRoleId, teachingReport);

        log.info("Upisano: {} zaposlenih, {} radnih mesta, {} naloga",
                workerRepository.count(), positionRepository.count(), userAccountRepository.count());
    }

    private Position position(String title, String grade, long salary) {
        Position position = new Position();
        position.setTitle(title);
        position.setSalaryGrade(grade);
        position.setBaseSalary(BigDecimal.valueOf(salary));
        position.setIsVacant(false);
        position.setCreatedAt(LocalDateTime.now());
        return positionRepository.save(position);
    }

    private Worker worker(String firstName, String lastName, String email, String personalId,
                          String phone, LocalDate hireDate) {
        Worker worker = new Worker();
        worker.setFirstName(firstName);
        worker.setLastName(lastName);
        worker.setEmail(email);
        worker.setPersonalId(personalId);
        worker.setPhone(phone);
        worker.setHireDate(hireDate);
        worker.setEmploymentStatus(EmploymentStatus.ACTIVE);
        worker.setEmploymentType(EmploymentType.FULL_TIME);
        return workerRepository.save(worker);
    }

    private void assign(Worker worker, Position position, LocalDate from) {
        WorkerPosition wp = new WorkerPosition();
        wp.setWorkerId(worker.getId());
        wp.setPositionId(position.getId());
        wp.setValidFrom(from);
        wp.setFraction(BigDecimal.ONE);
        wp.setIsPrimary(true);
        workerPositionRepository.save(wp);
    }

    private void user(String username, String password, Long workerId, Long roleId) {
        if (userAccountRepository.existsByUsername(username)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setWorkerId(workerId);
        user.setRoleId(roleId);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userAccountRepository.save(user);
    }

    private Long roleId(String code) {
        return roleRepository.findByCode(code).map(Role::getId).orElse(null);
    }

    private Permission permission(String code, String name, String module) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        permission.setModule(module);
        return permissionRepository.save(permission);
    }

    private void grant(Long roleId, Permission... permissions) {
        if (roleId == null) {
            return;
        }
        for (Permission permission : permissions) {
            RolePermission link = new RolePermission();
            link.setRoleId(roleId);
            link.setPermissionId(permission.getId());
            rolePermissionRepository.save(link);
        }
    }
}
