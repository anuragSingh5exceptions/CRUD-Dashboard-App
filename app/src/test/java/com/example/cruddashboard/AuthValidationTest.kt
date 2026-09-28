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
    fun `password reset requires every field`() {
        assertEquals("All fields are required", validateResetPasswordFields("", "password", "password"))
        assertEquals("All fields are required", validateResetPasswordFields("user@example.com", "", ""))
    }

    @Test
    fun `password reset validates email and password`() {
        assertEquals("Enter a valid email address", validateResetPasswordFields("invalid", "password", "password"))
        assertEquals("Password must be at least 6 characters", validateResetPasswordFields("user@example.com", "short", "short"))
        assertEquals("Passwords do not match", validateResetPasswordFields("user@example.com", "password", "different"))
    }

    @Test
    fun `password reset accepts valid matching passwords`() {
        assertNull(validateResetPasswordFields("user@example.com", "password", "password"))
    }
}
