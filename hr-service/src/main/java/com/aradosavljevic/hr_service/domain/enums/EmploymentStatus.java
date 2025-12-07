package com.aradosavljevic.hr_service.domain.enums;

import lombok.Getter;

@Getter
public enum EmploymentStatus {
    ACTIVE("active", "Active"),
    ON_LEAVE("on_leave", "On Leave"),
    TERMINATED("terminated", "Terminated"),
    SUSPENDED("suspended", "Suspended");

    private final String code;
    private final String displayName;

    EmploymentStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}
