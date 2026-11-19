package com.stockflow.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockflow.app.ui.components.EmptyState
import com.stockflow.app.ui.components.ProductGrid
import com.stockflow.app.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: ProductViewModel,
    bottomBar: @Composable () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val favorites by viewModel.favoriteProducts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Favorites") }) },
        bottomBar = bottomBar
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentFavorites = favorites
            when {
                currentFavorites == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                currentFavorites.isEmpty() -> EmptyState(
                    icon = Icons.Filled.FavoriteBorder,
                    title = "No favorites yet",
                    message = "Tap the heart on a product to keep it here for quick access."
                )

                else -> ProductGrid(
                    products = currentFavorites,
                    onProductClick = { product -> onProductClick(product.id) },
                    onToggleFavorite = { product -> viewModel.toggleFavorite(product) }
                )
            }
        }
    }
}
