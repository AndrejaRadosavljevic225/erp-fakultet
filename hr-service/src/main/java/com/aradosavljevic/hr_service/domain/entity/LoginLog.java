package com.aradosavljevic.hr_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDateTime loginTime;

    private LocalDateTime logoutTime;

    private String ipAddress;

    private String status;

    private String userAgent;

    // Helper methods
    public void recordLogout() {
        this.logoutTime = LocalDateTime.now();
    }

    public boolean isActive() {
        return logoutTime == null;
    }
}

