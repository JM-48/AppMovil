package com.example.catalogoproductos.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private fun clpFormatter(): NumberFormat {
        val clLocale = Locale.Builder().setLanguage("es").setRegion("CL").build()
        val format = NumberFormat.getCurrencyInstance(clLocale)
        // CLP no usa decimales
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        return format
    }

    fun formatCLP(value: Int): String = clpFormatter().format(value)

    fun formatCLP(value: Double): String = clpFormatter().format(value)
}
