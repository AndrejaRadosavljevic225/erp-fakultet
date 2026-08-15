package com.aradosavljevic.hr_service.infrastructure.security;

import com.aradosavljevic.hr_service.domain.entity.UserAccount;
import com.aradosavljevic.hr_service.domain.repository.RoleRepository;
import com.aradosavljevic.hr_service.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronadjen: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoleId() != null) {
            roleRepository.findById(user.getRoleId()).ifPresent(role -> {
                if (role.getCode() != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
                }
            });
        }

        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                Boolean.TRUE.equals(user.getIsActive()),
                true, true, true,
                authorities);
    }
}
