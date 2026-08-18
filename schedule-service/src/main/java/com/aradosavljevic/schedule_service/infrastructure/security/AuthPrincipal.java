package com.aradosavljevic.schedule_service.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Autentifikovani korisnik iz JWT-a: username, workerId (za "svoji podaci") i kod role.
 */
@Getter
@AllArgsConstructor
public class AuthPrincipal {
    private final String username;
    private final Long workerId;
    private final String roleCode;
}
