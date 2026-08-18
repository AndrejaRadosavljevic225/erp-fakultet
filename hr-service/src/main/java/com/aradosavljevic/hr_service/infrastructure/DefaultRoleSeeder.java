package com.aradosavljevic.hr_service.infrastructure;

import com.aradosavljevic.hr_service.domain.entity.Role;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Pravi difoltne role (ADMIN, HR, PROFESOR) pri pokretanju ako ne postoje,
 * i osigurava da 'admin' korisnik ima ADMIN rolu.
 */
@Component
@RequiredArgsConstructor
public class DefaultRoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role admin = ensureRole("ADMIN", "Administrator", "Pun pristup sistemu");
        ensureRole("HR", "HR sluzba", "Upravljanje kadrovima bez destruktivnih i RBAC akcija");
        ensureRole("PROFESOR", "Profesor", "Barata svojim podacima, ograniceni pregled tudjih");

        userAccountRepository.findByUsername("admin").ifPresent(u -> {
            if (!admin.getId().equals(u.getRoleId())) {
                u.setRoleId(admin.getId());
                userAccountRepository.save(u);
            }
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
