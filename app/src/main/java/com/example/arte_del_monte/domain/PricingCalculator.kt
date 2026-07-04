package com.example.arte_del_monte.domain

import com.example.arte_del_monte.ui.viewmodel.ItemUiState
import kotlin.math.roundToLong

object PricingCalculator {

    /** Subtotal de una línea: qty * price * (1 - discount/100) */
    fun lineSubtotal(qty: Double, price: Double, discountPercent: Double): Double {
        val base = qty * price
        return base * (1.0 - discountPercent / 100.0)
    }

    /** Suma de subtotales de todos los ítems */
    fun documentSubtotal(items: List<ItemUiState>): Double =
        items.sumOf { lineSubtotal(it.quantity, it.unitPrice, it.discountPercent) }

    /** Aplica descuento global al subtotal */
    fun afterGlobalDiscount(subtotal: Double, globalDiscountPercent: Double): Double =
        subtotal * (1.0 - globalDiscountPercent / 100.0)

    /** Aplica impuesto */
    fun applyTax(amount: Double, taxRate: Double): Double =
        amount * taxRate / 100.0

    /** Total final — retorna Triple(subtotalAfterDiscount, tax, grandTotal) */
    fun grandTotal(
        items: List<ItemUiState>,
        globalDiscountPercent: Double,
        taxRate: Double,
        taxEnabled: Boolean
    ): Triple<Double, Double, Double> {
        val subtotal     = documentSubtotal(items)
        val afterDiscount = afterGlobalDiscount(subtotal, globalDiscountPercent)
        val tax          = if (taxEnabled) applyTax(afterDiscount, taxRate) else 0.0
        val total        = afterDiscount + tax
        return Triple(afterDiscount, tax, total)
    }

    /** Formatea un Double como moneda colombiana (p. ej. $ 1.250.000) */
    fun formatCurrency(amount: Double): String {
        val long = amount.roundToLong()
        val str  = long.toString()
        val sb   = StringBuilder()
        str.reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) sb.append('.')
            sb.append(c)
        }
        return "$ " + sb.reverse().toString()
    }
}
