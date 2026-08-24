package com.aradosavljevic.hr_service.infrastructure;

import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Pravi difoltne role (ADMIN, HR, PROFESOR) i administratorski nalog pri pokretanju,
 * ako vec ne postoje. Bez ovoga bi na praznoj bazi sistem bio nedostupan — ne bi
 * postojao nijedan nalog kojim bi se korisnik prijavio.
 *
 * Lozinka administratora se uzima iz promenljive okruzenja ADMIN_PASSWORD;
 * dev vrednost je "admin123" i treba je promeniti pri prvoj prijavi.
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class DefaultRoleSeeder implements ApplicationRunner {

    public static final String ADMIN_USERNAME = "admin";

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-password:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role admin = ensureRole("ADMIN", "Administrator", "Pun pristup sistemu");
        ensureRole("HR", "HR sluzba", "Upravljanje kadrovima bez destruktivnih i RBAC akcija");
        ensureRole("PROFESOR", "Profesor", "Barata svojim podacima, ograniceni pregled tudjih");

        userAccountRepository.findByUsername(ADMIN_USERNAME)
                .ifPresentOrElse(
                        user -> {
                            if (!admin.getId().equals(user.getRoleId())) {
                                user.setRoleId(admin.getId());
                                userAccountRepository.save(user);
                            }
                        },
                        () -> {
                            UserAccount user = new UserAccount();
                            user.setUsername(ADMIN_USERNAME);
                            user.setPasswordHash(passwordEncoder.encode(adminPassword));
                            user.setRoleId(admin.getId());
                            user.setIsActive(true);
                            user.setCreatedAt(LocalDateTime.now());
                            userAccountRepository.save(user);
                            log.info("Kreiran administratorski nalog '{}' (promenite lozinku pri prvoj prijavi)",
                                    ADMIN_USERNAME);
                        });
    }

    private Role ensureRole(String code, String name, String description) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            role.setIsActive(true);
            role.setCreatedAt(LocalDateTime.now());
            return roleRepository.save(role);
        });
    }
}
