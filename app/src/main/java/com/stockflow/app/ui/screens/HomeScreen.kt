package com.stockflow.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockflow.app.data.ProductOptions
import com.stockflow.app.ui.components.CategoryFilterRow
import com.stockflow.app.ui.components.EmptyState
import com.stockflow.app.ui.components.ProductGrid
import com.stockflow.app.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ProductViewModel,
    bottomBar: @Composable () -> Unit,
    onProductClick: (Int) -> Unit,
    onAddProduct: () -> Unit
) {
    val products by viewModel.homeProducts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val filterOptions = listOf(ProductOptions.ALL_CATEGORIES) + ProductOptions.categories

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("StockFlow", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Clothing inventory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Product") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryFilterRow(
                categories = filterOptions,
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> viewModel.selectCategory(category) },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // "val" keeps a read-only local copy so Kotlin can smart-cast it.
            val currentProducts = products
            when {
                currentProducts == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                currentProducts.isEmpty() -> EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = if (selectedCategory == ProductOptions.ALL_CATEGORIES) "No products yet"
                    else "No products in $selectedCategory",
                    message = if (selectedCategory == ProductOptions.ALL_CATEGORIES)
                        "Tap \"Add Product\" to save your first clothing item."
                    else "Choose another category or add a new product."
                )

                else -> ProductGrid(
                    products = currentProducts,
                    onProductClick = { product -> onProductClick(product.id) },
                    onToggleFavorite = { product -> viewModel.toggleFavorite(product) },
                    // Extra bottom space so the last card is not hidden behind the button.
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp)
                )
            }
        }
    }
}
