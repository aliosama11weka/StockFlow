package com.stockflow.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stockflow.app.data.ProductOptions

/** A small pill that shows the quantity and warns when stock is low or empty. */
@Composable
fun StockBadge(quantity: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    // "when" picks the label and colours for each stock level.
    val (label, background, content) = when {
        quantity == 0 -> Triple("Out of stock", colors.errorContainer, colors.onErrorContainer)
        quantity <= ProductOptions.LOW_STOCK_LIMIT ->
            Triple("Low · $quantity left", colors.secondaryContainer, colors.onSecondaryContainer)
        else -> Triple("$quantity in stock", colors.primaryContainer, colors.onPrimaryContainer)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = background,
        contentColor = content
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
