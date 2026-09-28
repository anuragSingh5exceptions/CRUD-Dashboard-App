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
}
