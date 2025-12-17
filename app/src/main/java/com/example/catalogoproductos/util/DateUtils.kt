package com.example.catalogoproductos.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "N/A"
        try {
            // Asumiendo formato ISO 8601
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoString) ?: return isoString
            
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return formatter.format(date)
        } catch (e: Exception) {
            return isoString
        }
    }
}
