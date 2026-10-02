package com.example.deliverlk.core.util

import java.text.NumberFormat
import java.util.Locale

object Formatters {
    fun formatLkr(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return "Rs. ${formatter.format(amount)}"
    }

    fun formatDistance(km: Double): String {
        return String.format(Locale.US, "%.1f km", km)
    }

    fun isValidPhone(phone: String): Boolean {
        // Accepts Sri Lankan format: +94 7X XXX XXXX or 07XXXXXXXX or +947XXXXXXXX
        val cleaned = phone.replace("\\s+".toRegex(), "")
        return cleaned.matches("^(\\+94|0)?7[0-9]{8}$".toRegex())
    }

    fun formatPhone(phone: String): String {
        val cleaned = phone.replace("\\s+".toRegex(), "").replace("^0".toRegex(), "+94")
        val formatted = if (!cleaned.startsWith("+94")) "+94$cleaned" else cleaned
        if (formatted.length == 12) {
            return "${formatted.substring(0, 3)} ${formatted.substring(3, 5)} ${formatted.substring(5, 8)} ${formatted.substring(8)}"
        }
        return phone
    }
}
