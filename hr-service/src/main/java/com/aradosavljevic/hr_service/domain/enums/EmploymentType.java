package com.aradosavljevic.hr_service.domain.enums;

import lombok.Getter;

@Getter
public enum EmploymentType {
    FULL_TIME("full_time", "Full Time"),
    PART_TIME("part_time", "Part Time"),
    CONTRACT("contract", "Contract"),
    TEMPORARY("temporary", "Temporary");

    private final String code;
    private final String displayName;

    EmploymentType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}
