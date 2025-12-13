package com.example.catalogoproductos.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

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

    fun netFromGrossCLP(gross: Int, ivaRate: Double = 0.19): Int {
        val net = (gross / (1.0 + ivaRate)).roundToInt()
        return net
    }

    fun taxFromGrossCLP(gross: Int, ivaRate: Double = 0.19): Int {
        val net = netFromGrossCLP(gross, ivaRate)
        return gross - net
    }

    fun formatNetFromGrossCLP(gross: Int, ivaRate: Double = 0.19): String = formatCLP(netFromGrossCLP(gross, ivaRate))
    fun formatTaxFromGrossCLP(gross: Int, ivaRate: Double = 0.19): String = formatCLP(taxFromGrossCLP(gross, ivaRate))
}

