package com.stockflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage

/**
 * Shows the product photo if one was saved, otherwise a coloured tile
 * with the product's first letter.
 */
@Composable
fun ProductImage(
    imageUri: String?,
    productName: String,
    modifier: Modifier = Modifier,
    letterStyle: TextStyle = MaterialTheme.typography.titleLarge
) {
    if (imageUri != null) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Photo of $productName",
            contentScale = ContentScale.Crop,
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = productName.trim().take(1).uppercase().ifEmpty { "?" },
                style = letterStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
