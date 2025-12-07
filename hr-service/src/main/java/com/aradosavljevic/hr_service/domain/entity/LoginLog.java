package com.aradosavljevic.hr_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_log", indexes = {
        @Index(name = "idx_login_log_user", columnList = "user_id"),
        @Index(name = "idx_login_log_time", columnList = "login_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "login_id")
    private UUID loginId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // Helper methods
    public void recordLogout() {
        this.logoutTime = LocalDateTime.now();
    }

    public boolean isActive() {
        return logoutTime == null;
    }
}

