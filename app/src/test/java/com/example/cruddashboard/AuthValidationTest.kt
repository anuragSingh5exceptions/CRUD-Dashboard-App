package com.example.cruddashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun `sign up requires every field`() {
        assertEquals("All fields are required", validateSignUpFields("", "user@example.com", "password"))
        assertEquals("All fields are required", validateSignUpFields("user", "", "password"))
        assertEquals("All fields are required", validateSignUpFields("user", "user@example.com", ""))
    }

    @Test
    fun `sign up accepts non-empty fields`() {
        assertNull(validateSignUpFields("user", "user@example.com", "password"))
    }

    @Test
    fun `admin configuration requires every field`() {
        assertEquals("All fields are required", validateAdminConfigFields("", "admin", "password"))
        assertEquals("All fields are required", validateAdminConfigFields("https://example.com", "", "password"))
        assertEquals("All fields are required", validateAdminConfigFields("https://example.com", "admin", ""))
    }

    @Test
    fun `admin configuration accepts non-empty fields`() {
        assertNull(validateAdminConfigFields("https://example.com", "admin", "password"))
    fun `password reset requires every field`() {
        assertEquals("All fields are required", validatePasswordResetFields("", "password", "password"))
        assertEquals("All fields are required", validatePasswordResetFields("user@example.com", "", ""))
    }

    @Test
    fun `password reset requires a sufficiently long matching password`() {
        assertEquals("Password must be at least 6 characters", validatePasswordResetFields("user@example.com", "short", "short"))
        assertEquals("Passwords do not match", validatePasswordResetFields("user@example.com", "password", "different"))
        assertNull(validatePasswordResetFields("user@example.com", "password", "password"))
    }
}
