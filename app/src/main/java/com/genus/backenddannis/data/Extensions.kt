package com.genus.backenddannis.data

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun Double.toRupiahFormat(): String {
    val formatter = NumberFormat.getInstance(Locale("id", "ID"))
    if (formatter is DecimalFormat) {
        formatter.applyPattern("#,###,###,###")
    }
    return formatter.format(this)
}
