package com.example.catalogoproductos

import com.example.catalogoproductos.util.CurrencyUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyUtilsTest {
    @Test
    fun `formatea CLP entero sin decimales`() {
        val s = CurrencyUtils.formatCLP(10000)
        assertTrue(s.contains("10.000"))
    }

    @Test
    fun `formatea CLP double sin decimales`() {
        val s = CurrencyUtils.formatCLP(19999.99)
        assertTrue(s.contains("20.000"))
    }

    @Test
    fun `net y tax desde total con IVA`() {
        val gross = 11900
        val net = CurrencyUtils.netFromGrossCLP(gross)
        val tax = CurrencyUtils.taxFromGrossCLP(gross)
        assertTrue(net == 10000)
        assertTrue(tax == 1900)
        val netFmt = CurrencyUtils.formatNetFromGrossCLP(gross)
        val taxFmt = CurrencyUtils.formatTaxFromGrossCLP(gross)
        assertTrue(netFmt.contains("10.000"))
        assertTrue(taxFmt.contains("1.900"))
    }
}
