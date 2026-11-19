package com.stockflow.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stockflow.app.data.Product
import com.stockflow.app.data.ProductOptions
import com.stockflow.app.navigation.Screen
import com.stockflow.app.ui.components.ProductImage
import com.stockflow.app.util.ImageStorage
import com.stockflow.app.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

/**
 * One form for both "Add Product" and "Edit Product".
 * [productId] is [Screen.ProductForm.NEW_PRODUCT] when adding.
 * [onSaved] receives true when a new product was created.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Int,
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onSaved: (isNewProduct: Boolean) -> Unit
) {
    val isEditing = productId != Screen.ProductForm.NEW_PRODUCT
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form fields. "var ... by rememberSaveable" keeps them when the screen rotates.
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var sku by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var size by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var quantityText by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }

    // Values kept from the product being edited.
    var originalImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var isFavorite by rememberSaveable { mutableStateOf(false) }
    var isLoaded by rememberSaveable { mutableStateOf(!isEditing) }

    var showErrors by rememberSaveable { mutableStateOf(false) }
    var skuError by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Pre-fill the form once when editing.
    LaunchedEffect(productId) {
        if (isEditing && !isLoaded) {
            val product = viewModel.loadProduct(productId)
            if (product != null) {
                name = product.name
                category = product.category
                sku = product.sku
                color = product.color
                size = product.size
                description = product.description
                quantityText = product.quantity.toString()
                priceText = product.sellingPrice.toBigDecimal().stripTrailingZeros().toPlainString()
                imageUri = product.imageUri
                originalImageUri = product.imageUri
                isFavorite = product.isFavorite
            }
            isLoaded = true
        }
    }

    // Optional photo: Android's photo picker, then a private copy of the file.
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val savedUri = ImageStorage.copyToAppStorage(context, uri)
                if (savedUri == null) {
                    snackbarHostState.showSnackbar("Could not load that image")
                } else {
                    if (imageUri != originalImageUri) ImageStorage.deleteImage(context, imageUri)
                    imageUri = savedUri
                }
            }
        }
    }

    // Cancel: throw away a photo picked during this visit, then leave.
    val cancel: () -> Unit = {
        if (imageUri != originalImageUri) ImageStorage.deleteImage(context, imageUri)
        onBack()
    }
    BackHandler(onBack = cancel)

    // Validation rules. Each value is null when the field is valid.
    val quantity = quantityText.toIntOrNull()
    val price = priceText.toDoubleOrNull()
    val nameError = if (name.isBlank()) "Product name is required" else null
    val categoryError = if (category.isBlank()) "Choose a category" else null
    val skuRequiredError = if (sku.isBlank()) "SKU is required" else null
    val colorError = if (color.isBlank()) "Color is required" else null
    val sizeError = if (size.isBlank()) "Choose a size" else null
    val quantityError = when {
        quantityText.isBlank() -> "Quantity is required"
        quantity == null -> "Enter a whole number"
        quantity < 0 -> "Quantity cannot be negative"
        else -> null
    }
    val priceError = when {
        priceText.isBlank() -> "Selling price is required"
        price == null -> "Enter a valid price, e.g. 249.99"
        price < 0 -> "Price cannot be negative"
        else -> null
    }
    val formIsValid = listOf(nameError, categoryError, skuRequiredError, colorError, sizeError, quantityError, priceError)
        .all { it == null }

    val save: () -> Unit = save@{
        showErrors = true
        if (!formIsValid || isSaving) return@save
        isSaving = true
        val product = Product(
            id = if (isEditing) productId else 0,
            name = name.trim(),
            sku = sku.trim().uppercase(),
            category = category,
            color = color.trim(),
            size = size,
            description = description.trim(),
            quantity = quantity ?: 0,
            sellingPrice = price ?: 0.0,
            isFavorite = isFavorite,
            imageUri = imageUri
        )
        viewModel.saveProduct(
            product = product,
            onSuccess = {
                // The old photo is no longer used once a new one is saved.
                if (originalImageUri != imageUri) ImageStorage.deleteImage(context, originalImageUri)
                onSaved(!isEditing)
            },
            onError = { message ->
                isSaving = false
                skuError = message
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Product" else "Add Product") },
                navigationIcon = {
                    IconButton(onClick = cancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = save, enabled = isLoaded && !isSaving) { Text("Save") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (!isLoaded) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ---------- Photo (optional) ----------
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProductImage(
                        imageUri = imageUri,
                        productName = name.ifBlank { "+" },
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        OutlinedButton(onClick = {
                            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) { Text(if (imageUri == null) "Add photo" else "Change photo") }
                        if (imageUri != null) {
                            TextButton(onClick = {
                                if (imageUri != originalImageUri) ImageStorage.deleteImage(context, imageUri)
                                imageUri = null
                            }) { Text("Remove photo") }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // ---------- Text fields ----------
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Product name",
                    error = nameError.takeIf { showErrors },
                    capitalization = KeyboardCapitalization.Words
                )
                DropdownField(
                    label = "Category",
                    options = ProductOptions.categories,
                    selected = category,
                    onSelected = { category = it },
                    error = categoryError.takeIf { showErrors }
                )
                FormTextField(
                    value = sku,
                    onValueChange = { text ->
                        sku = text
                        skuError = null
                    },
                    label = "SKU (e.g. TS-001)",
                    error = (skuRequiredError ?: skuError).takeIf { showErrors },
                    capitalization = KeyboardCapitalization.Characters
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = "Color",
                        error = colorError.takeIf { showErrors },
                        capitalization = KeyboardCapitalization.Words,
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = "Size",
                        options = ProductOptions.sizes,
                        selected = size,
                        onSelected = { size = it },
                        error = sizeError.takeIf { showErrors },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        value = quantityText,
                        onValueChange = { text -> quantityText = text.filter { it.isDigit() }.take(6) },
                        label = "Quantity",
                        error = quantityError.takeIf { showErrors },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    FormTextField(
                        value = priceText,
                        onValueChange = { text -> priceText = text.filter { it.isDigit() || it == '.' }.take(10) },
                        label = "Selling price (${ProductOptions.CURRENCY})",
                        error = priceError.takeIf { showErrors },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
                FormTextField(
                    value = description,
                    onValueChange = { description = it.take(300) },
                    label = "Short description (optional)",
                    error = null,
                    singleLine = false,
                    capitalization = KeyboardCapitalization.Sentences
                )

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = cancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = save, enabled = !isSaving, modifier = Modifier.weight(1f)) {
                        Text(if (isEditing) "Save changes" else "Save product")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** A read-only text field that opens a list of fixed options. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (option == selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
