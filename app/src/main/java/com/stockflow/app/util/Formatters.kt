package com.stockflow.app.util

import com.stockflow.app.data.ProductOptions
import java.util.Locale

/** Formats a price such as 349.5 as "EGP 349.50". */
fun formatPrice(price: Double): String =
    "${ProductOptions.CURRENCY} ${String.format(Locale.US, "%.2f", price)}"
