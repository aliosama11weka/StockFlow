package com.example.stockflow

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddEditProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val dao = db.appDao()
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val isEditing = productId != -1

        setContent {
          StockFlowTheme {
            var existingProduct by remember { mutableStateOf<Product?>(null) }

            LaunchedEffect(productId) {
                if (isEditing) {
                    existingProduct = dao.getProductByIdOnce(productId)
                }
            }

            if (isEditing && existingProduct == null) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else {
                AddEditProductScreen(
                    existingProduct = existingProduct,
                    onSave = { product ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val skuTaken = dao.countProductsWithSku(product.sku, product.id) > 0
                            if (skuTaken) {
                                launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@AddEditProductActivity,
                                        "SKU \"${product.sku}\" is already used",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                return@launch
                            }
                            if (isEditing) {
                                dao.updateProduct(product)
                            } else {
                                dao.insertProduct(product)
                            }
                            launch(Dispatchers.Main) { finish() }
                        }
                    },
                    onCancel = { finish() }
                )
            }
          }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    existingProduct: Product?,
    onSave: (Product) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = existingProduct != null
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var sku by remember { mutableStateOf(existingProduct?.sku ?: "") }
    var category by remember { mutableStateOf(existingProduct?.category ?: ProductOptions.categories.first()) }
    var color by remember { mutableStateOf(existingProduct?.color ?: "") }
    var size by remember { mutableStateOf(existingProduct?.size ?: ProductOptions.sizes[2]) }
    var quantityText by remember { mutableStateOf(existingProduct?.quantity?.toString() ?: "0") }
    var priceText by remember { mutableStateOf(existingProduct?.sellingPrice?.toString() ?: "") }
    var description by remember { mutableStateOf(existingProduct?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(existingProduct?.imageUri?.let { Uri.parse(it) }) }
    var showErrors by remember { mutableStateOf(false) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var sizeExpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Product" else "Add Product") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product image preview + pick button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.85f))
                ) {
                    Text(if (imageUri == null) "Upload Image" else "Change Image")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                isError = showErrors && name.isBlank(),
                supportingText = { if (showErrors && name.isBlank()) Text("Required") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it.uppercase() },
                label = { Text("SKU") },
                isError = showErrors && sku.isBlank(),
                supportingText = { if (showErrors && sku.isBlank()) Text("Required") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    ProductOptions.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { category = cat; categoryExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color") },
                isError = showErrors && color.isBlank(),
                supportingText = { if (showErrors && color.isBlank()) Text("Required") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Size dropdown
            ExposedDropdownMenuBox(
                expanded = sizeExpanded,
                onExpandedChange = { sizeExpanded = it }
            ) {
                OutlinedTextField(
                    value = size,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Size") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sizeExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = sizeExpanded, onDismissRequest = { sizeExpanded = false }) {
                    ProductOptions.sizes.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = { size = s; sizeExpanded = false }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' }.take(10) },
                    label = { Text("Price (EGP)") },
                    isError = showErrors && priceText.isBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(300) },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 4
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        showErrors = true
                        if (name.isBlank() || sku.isBlank() || color.isBlank() || priceText.isBlank()) return@Button
                        val product = Product(
                            id = existingProduct?.id ?: 0,
                            name = name.trim(),
                            sku = sku.trim(),
                            category = category,
                            color = color.trim(),
                            size = size,
                            quantity = quantityText.toIntOrNull() ?: 0,
                            sellingPrice = priceText.toDoubleOrNull() ?: 0.0,
                            description = description.trim(),
                            isFavorite = existingProduct?.isFavorite ?: false,
                            imageUri = imageUri?.toString()
                        )
                        onSave(product)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text(if (isEditing) "Save Changes" else "Save Product")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
