package com.aradosavljevic.schedule_service.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Pomocne metode za citanje trenutno ulogovanog korisnika iz SecurityContext-a.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal) {
            return principal;
        }
        return null;
    }

    public static Long currentWorkerId() {
        AuthPrincipal principal = currentPrincipal();
        return principal != null ? principal.getWorkerId() : null;
    }

    /** ADMIN ili HR — smeju da rade sa tudjim podacima (bez vlasnickog ogranicenja). */
    public static boolean isPrivileged() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_HR"));
    }
}
