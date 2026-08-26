package com.kfpcl.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    private PhoneValidator phoneValidator;
    private GstinValidator gstinValidator;
    private PanValidator panValidator;

    @BeforeEach
    void setUp() {
        phoneValidator = new PhoneValidator();
        gstinValidator = new GstinValidator();
        panValidator = new PanValidator();
    }

    @Test
    @DisplayName("Should validate correct 10-digit Indian phone numbers")
    void testPhoneValidation_Valid() {
        assertTrue(phoneValidator.isValid("9876543210", null));
        assertTrue(phoneValidator.isValid("8123456789", null));
        assertTrue(phoneValidator.isValid("7012345678", null));
        assertTrue(phoneValidator.isValid("6345678901", null));
    }

    @Test
    @DisplayName("Should reject invalid phone numbers")
    void testPhoneValidation_Invalid() {
        assertFalse(phoneValidator.isValid("1234567890", null)); // starts with 1
        assertFalse(phoneValidator.isValid("987654321", null));   // 9 digits
        assertFalse(phoneValidator.isValid("98765432100", null)); // 11 digits
        assertFalse(phoneValidator.isValid("98765abcde", null)); // letters
        assertFalse(phoneValidator.isValid(null, null));
        assertFalse(phoneValidator.isValid("", null));
    }

    @Test
    @DisplayName("Should validate correct 15-character Indian GSTIN")
    void testGstinValidation_Valid() {
        assertTrue(gstinValidator.isValid("27AABCA1234F1Z1", null));
        assertTrue(gstinValidator.isValid("29ABCDE1234F2Z5", null));
        assertTrue(gstinValidator.isValid("07AAAAA0000A1Z5", null));
    }

    @Test
    @DisplayName("Should reject invalid GSTIN format")
    void testGstinValidation_Invalid() {
        assertFalse(gstinValidator.isValid("27AABCA1234F", null)); // short
        assertFalse(gstinValidator.isValid("27AABCA1234F1Z199", null)); // too long
        assertFalse(gstinValidator.isValid("INVALIDGSTIN123", null));
        assertFalse(gstinValidator.isValid(null, null));
        assertFalse(gstinValidator.isValid("", null));
    }

    @Test
    @DisplayName("Should validate correct 10-character Indian PAN")
    void testPanValidation_Valid() {
        assertTrue(panValidator.isValid("AABCA1234F", null));
        assertTrue(panValidator.isValid("ABCDE1234F", null));
        assertTrue(panValidator.isValid("XYZPA9876Q", null));
    }

    @Test
    @DisplayName("Should reject invalid PAN format")
    void testPanValidation_Invalid() {
        assertFalse(panValidator.isValid("AABC1234F", null)); // 9 chars
        assertFalse(panValidator.isValid("AABCA12345F", null)); // 11 chars
        assertFalse(panValidator.isValid("12345ABCDE", null)); // numbers first
        assertFalse(panValidator.isValid(null, null));
        assertFalse(panValidator.isValid("", null));
    }
}
