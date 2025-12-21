package com.aradosavljevic.hr_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String passwordHash;

    private Long workerId;

    private Long roleId;

    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;


    // Helper methods
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
