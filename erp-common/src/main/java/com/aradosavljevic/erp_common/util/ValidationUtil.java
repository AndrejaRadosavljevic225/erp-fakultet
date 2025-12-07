package com.aradosavljevic.erp_common.util;



import com.aradosavljevic.erp_common.exception.ValidationException;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9\\s-]{9,15}$");

    private static final Pattern JMBG_PATTERN =
            Pattern.compile("^\\d{13}$");

    private ValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email", "Invalid email format");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("phone", "Invalid phone format");
        }
    }

    public static void validateJMBG(String jmbg) {
        if (jmbg == null || !JMBG_PATTERN.matcher(jmbg).matches()) {
            throw new ValidationException("personalId", "JMBG must be exactly 13 digits");
        }
    }

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
    }

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty");
        }
    }

    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be positive");
        }
    }
}

