package com.example.cruddashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cruddashboard.data.CustomerRepository
import com.example.cruddashboard.model.Customer
import com.example.cruddashboard.model.CustomerStatus

class AppController(private val repository: CustomerRepository) {
    val customers = mutableStateListOf<Customer>().apply { addAll(repository.loadCustomers()) }
    var isLoggedIn by mutableStateOf(repository.isLoggedIn)
        private set
    var searchQuery by mutableStateOf("")

    val filteredCustomers: List<Customer>
        get() {
            val query = searchQuery.trim()
            return if (query.isBlank()) customers else customers.filter {
                it.name.contains(query, true) || it.email.contains(query, true) ||
                    it.company.contains(query, true) || it.status.name.contains(query, true)
            }
        }

    fun login(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) return "Enter your email and password"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Enter a valid email address"
        if (password.length < 6) return "Password must be at least 6 characters"
        val savedPassword = repository.passwordFor(email)
        if (savedPassword != null && savedPassword != password) return "Incorrect email or password"
        repository.isLoggedIn = true
        isLoggedIn = true
        return null
    }

    fun signUp(username: String, email: String, password: String): String? {
        validateSignUpFields(username, email, password)?.let { return it }
        return login(email, password)
    }

    fun resetPassword(email: String, newPassword: String, confirmPassword: String): String? {
        validateResetPasswordFields(email, newPassword, confirmPassword)?.let { return it }
        repository.savePassword(email, newPassword)
        return null
    }

    fun logout() {
        repository.isLoggedIn = false
        isLoggedIn = false
        searchQuery = ""
    }

    fun saveCustomer(
        existingId: Long?, name: String, email: String, phone: String,
        company: String, status: CustomerStatus
    ): String? {
        if (name.trim().length < 2) return "Name must have at least 2 characters"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Enter a valid email address"
        if (company.isBlank()) return "Company is required"
        if (existingId == null && customers.any { it.email.equals(email.trim(), true) }) return "A customer with this email already exists"
        if (existingId != null && customers.any { it.id != existingId && it.email.equals(email.trim(), true) }) return "A customer with this email already exists"

        val old = existingId?.let { id -> customers.firstOrNull { it.id == id } }
        val customer = Customer(
            id = existingId ?: (customers.maxOfOrNull { it.id } ?: 0L) + 1,
            name = name.trim(), email = email.trim(), phone = phone.trim(), company = company.trim(),
            status = status, createdAt = old?.createdAt ?: System.currentTimeMillis()
        )
        if (old == null) customers.add(0, customer) else customers[customers.indexOf(old)] = customer
        repository.saveCustomers(customers)
        return null
    }

    fun deleteCustomer(customer: Customer) {
        customers.remove(customer)
        repository.saveCustomers(customers)
    }
}

internal fun validateSignUpFields(username: String, email: String, password: String): String? =
    if (username.isBlank() || email.isBlank() || password.isBlank()) "All fields are required" else null

internal fun validateResetPasswordFields(email: String, newPassword: String, confirmPassword: String): String? {
    if (email.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) return "All fields are required"
    if (!EMAIL_ADDRESS_REGEX.matches(email.trim())) return "Enter a valid email address"
    if (newPassword.length < 6) return "Password must be at least 6 characters"
    if (newPassword != confirmPassword) return "Passwords do not match"
    return null
}

private val EMAIL_ADDRESS_REGEX = Regex(
    pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
    option = RegexOption.IGNORE_CASE
)
