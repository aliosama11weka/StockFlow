package com.stockflow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockflow.app.data.Product
import com.stockflow.app.ui.components.ProductImage
import com.stockflow.app.ui.components.StockBadge
import com.stockflow.app.ui.theme.SkuTextStyle
import com.stockflow.app.util.ImageStorage
import com.stockflow.app.util.ShareHelper
import com.stockflow.app.util.formatPrice
import com.stockflow.app.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: Int,
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    // Observe this product in Room: edits, favorites and stock changes appear immediately.
    val productFlow = remember(productId) { viewModel.getProduct(productId) }
    val product by productFlow.collectAsStateWithLifecycle(initialValue = null)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // A read-only snapshot for this composition, so Kotlin can smart-cast it.
    val current = product

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (current != null) {
                        IconButton(onClick = { viewModel.toggleFavorite(current) }) {
                            Icon(
                                imageVector = if (current.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (current.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (current.isFavorite) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onEdit(current.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit product")
                        }
                        IconButton(onClick = { ShareHelper.shareProduct(context, current) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share product")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete product")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                ProductDetailsContent(
                    product = current,
                    onStockChange = { change ->
                        val succeeded = viewModel.adjustStock(current, change)
                        val message = when {
                            !succeeded -> "Not enough stock: only ${current.quantity} available"
                            change > 0 -> "Stock in: added $change"
                            else -> "Stock out: removed ${-change}"
                        }
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                    // On wide screens the content stays a comfortable reading width.
                    modifier = Modifier.widthIn(max = 640.dp)
                )
            }
        }
    }

    if (showDeleteDialog && current != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete product?") },
            text = { Text("\"${current.name}\" (${current.sku}) will be permanently removed from your inventory.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        ImageStorage.deleteImage(context, current.imageUri)
                        viewModel.deleteProduct(current) { onBack() }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProductDetailsContent(
    product: Product,
    onStockChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (product.imageUri != null) {
            ProductImage(
                imageUri = product.imageUri,
                productName = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Column {
            Text(product.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "SKU ${product.sku}",
                style = SkuTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            StockBadge(quantity = product.quantity)
        }

        DetailsCard {
            DetailRow("Category", product.category)
            DetailRow("Color", product.color)
            DetailRow("Size", product.size)
            DetailRow("Available quantity", product.quantity.toString())
            DetailRow("Selling price", formatPrice(product.sellingPrice))
        }

        DetailsCard {
            Text("Description", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                text = product.description.ifBlank { "No description added." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        StockAdjustmentCard(currentQuantity = product.quantity, onStockChange = onStockChange)
    }
}

/** Stock In / Stock Out controls. Stock Out can never go below zero. */
@Composable
private fun StockAdjustmentCard(currentQuantity: Int, onStockChange: (Int) -> Unit) {
    var amountText by rememberSaveable { mutableStateOf("1") }
    val amount = amountText.toIntOrNull()
    val isAmountValid = amount != null && amount > 0
    val canStockOut = amount != null && amount > 0 && amount <= currentQuantity

    DetailsCard {
        Text("Stock adjustment", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = { text -> amountText = text.filter { it.isDigit() }.take(6) },
            label = { Text("Quantity to add or remove") },
            singleLine = true,
            isError = !isAmountValid,
            supportingText = {
                when {
                    !isAmountValid -> Text("Enter a whole number greater than 0")
                    !canStockOut -> Text("Stock Out is limited to the $currentQuantity units available")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onStockChange(amount ?: 0) },
                enabled = isAmountValid,
                modifier = Modifier.weight(1f)
            ) { Text("+  Stock In") }
            OutlinedButton(
                onClick = { onStockChange(-(amount ?: 0)) },
                enabled = canStockOut,
                modifier = Modifier.weight(1f)
            ) { Text("−  Stock Out") }
        }
    }
}

@Composable
private fun DetailsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
