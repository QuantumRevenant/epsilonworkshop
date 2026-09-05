/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util;

/**
 *
 * @author sebap
 */

import com.epsilonworkshop.model.DocumentType;
import java.time.Year;
import java.util.regex.Pattern;

public class Validations {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final Pattern DOCUMENT_PATTERN = Pattern.compile(
            "^\\d+$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9+()\\-\\s]+$"
    );

    private static final Pattern PLATE_PATTERN = Pattern.compile(
            "^[A-Z0-9]{3}-?[A-Z0-9]{3}$"
    );

    private Validations() {
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }

    public static boolean isValidEmail(String email) {
        return isNotBlank(email)
                && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidDocument(
            DocumentType documentType,
            String document
    ) {
        if (documentType == null || !isNotBlank(document)) {
            return false;
        }

        if (!DOCUMENT_PATTERN.matcher(document).matches()) {
            return false;
        }

        return switch (documentType) {
            case DNI -> document.length() == 8;
            case CE -> document.length() == 9;
            case RUC -> document.length() == 11;
        };
    }

    public static boolean isValidPhone(String phone) {
        return isNotBlank(phone)
                && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPlate(String plate) {
        return isNotBlank(plate)
                && PLATE_PATTERN.matcher(plate.toUpperCase()).matches();
    }

    public static boolean isValidYear(Integer year) {
        if (year == null) {
            return true;
        }

        int currentYear = Year.now().getValue();

        return year >= 1886 && year <= currentYear;
    }

    public static boolean isValidPassword(String password) {
        return isNotBlank(password)
                && password.length() >= 8
                && password.length() <= 72;
    }
    
}
