package com.example.cruddashboard.data

import android.content.Context
import com.example.cruddashboard.model.Customer
import com.example.cruddashboard.model.CustomerStatus
import org.json.JSONArray
import org.json.JSONObject

class CustomerRepository(context: Context) {
    private val preferences = context.getSharedPreferences("cliently_data", Context.MODE_PRIVATE)

    fun loadCustomers(): List<Customer> {
        val raw = preferences.getString(KEY_CUSTOMERS, null) ?: return starterCustomers()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                Customer(
                    id = item.getLong("id"),
                    name = item.getString("name"),
                    email = item.getString("email"),
                    phone = item.optString("phone"),
                    company = item.optString("company"),
                    status = CustomerStatus.valueOf(item.optString("status", "LEAD")),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            }
        }.getOrElse { starterCustomers() }
    }

    fun saveCustomers(customers: List<Customer>) {
        val array = JSONArray()
        customers.forEach { customer ->
            array.put(JSONObject().apply {
                put("id", customer.id)
                put("name", customer.name)
                put("email", customer.email)
                put("phone", customer.phone)
                put("company", customer.company)
                put("status", customer.status.name)
                put("createdAt", customer.createdAt)
            })
        }
        preferences.edit().putString(KEY_CUSTOMERS, array.toString()).apply()
    }

    var isLoggedIn: Boolean
        get() = preferences.getBoolean(KEY_SESSION, false)
        set(value) { preferences.edit().putBoolean(KEY_SESSION, value).apply() }

    fun passwordFor(email: String): String? =
        if (preferences.getString(KEY_RESET_EMAIL, null)?.equals(email.trim(), ignoreCase = true) == true) {
            preferences.getString(KEY_RESET_PASSWORD, null)
        } else {
            null
        }

    fun savePassword(email: String, password: String) {
        preferences.edit()
            .putString(KEY_RESET_EMAIL, email.trim())
            .putString(KEY_RESET_PASSWORD, password)
            .apply()
    }

    private fun starterCustomers() = listOf(
        Customer(1, "Aarav Sharma", "aarav@brightlabs.in", "+91 98765 43210", "Bright Labs", CustomerStatus.ACTIVE),
        Customer(2, "Meera Kapoor", "meera@northstar.co", "+91 99887 76655", "Northstar", CustomerStatus.LEAD),
        Customer(3, "Rohan Iyer", "rohan@pixelcraft.io", "+91 91234 56789", "PixelCraft", CustomerStatus.ACTIVE),
        Customer(4, "Diya Patel", "diya@studioseven.in", "+91 90000 11122", "Studio Seven", CustomerStatus.INACTIVE)
    )

    companion object {
        private const val KEY_CUSTOMERS = "customers"
        private const val KEY_SESSION = "logged_in"
        private const val KEY_RESET_EMAIL = "reset_email"
        private const val KEY_RESET_PASSWORD = "reset_password"
    }
}
