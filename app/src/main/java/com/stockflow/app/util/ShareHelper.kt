package com.stockflow.app.util

import android.content.Context
import android.content.Intent
import com.stockflow.app.data.Product

/** Shares a short text summary of a product using Android's ACTION_SEND Intent. */
object ShareHelper {

    fun buildSummary(product: Product): String = buildString {
        appendLine("StockFlow product")
        appendLine("Name: ${product.name}")
        appendLine("SKU: ${product.sku}")
        appendLine("Category: ${product.category}")
        appendLine("Color: ${product.color}")
        appendLine("Size: ${product.size}")
        appendLine("Price: ${formatPrice(product.sellingPrice)}")
        append("In stock: ${product.quantity}")
        if (product.description.isNotBlank()) {
            appendLine()
            append("Description: ${product.description}")
        }
    }

    fun shareProduct(context: Context, product: Product) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${product.name} (${product.sku})")
            putExtra(Intent.EXTRA_TEXT, buildSummary(product))
        }
        // The chooser lets the user pick any compatible app (messages, email, notes...).
        context.startActivity(Intent.createChooser(sendIntent, "Share product via"))
    }
}
