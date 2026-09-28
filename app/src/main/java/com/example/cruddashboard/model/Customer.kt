package com.example.cruddashboard.model

data class Customer(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val company: String,
    val status: CustomerStatus,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CustomerStatus { ACTIVE, LEAD, INACTIVE }
