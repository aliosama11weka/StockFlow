package com.stockflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stockflow.app.StockFlowApplication
import com.stockflow.app.data.Product
import com.stockflow.app.data.ProductOptions
import com.stockflow.app.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds the product data shown by the screens and performs every
 * product action (create, update, delete, favorite, stock changes).
 * The screens only call these functions; they never touch Room directly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // ---------- Home: category filter ----------
    private val _selectedCategory = MutableStateFlow(ProductOptions.ALL_CATEGORIES)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    /** null means "still loading"; an empty list means "no products". */
    val homeProducts: StateFlow<List<Product>?> = _selectedCategory
        .flatMapLatest { category -> repository.productsInCategory(category) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // ---------- Search ----------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Product>> = _searchQuery
        .flatMapLatest { query ->
            val cleanQuery = query.trim()
            if (cleanQuery.isEmpty()) flowOf(emptyList()) else repository.searchProducts(cleanQuery)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ---------- Favorites ----------
    val favoriteProducts: StateFlow<List<Product>?> = repository.favoriteProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ---------- Single product ----------
    fun getProduct(id: Int): Flow<Product?> = repository.getProduct(id)

    suspend fun loadProduct(id: Int): Product? = repository.getProductOnce(id)

    /**
     * Inserts a new product (id == 0) or updates an existing one.
     * The result is reported back through the two lambda parameters.
     */
    fun saveProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (repository.isSkuTaken(product.sku, excludeId = product.id)) {
                onError("SKU \"${product.sku}\" is already used by another product")
                return@launch
            }
            if (product.id == 0) {
                repository.insert(product)
                // Show every category so the newly added product is visible at the top.
                _selectedCategory.value = ProductOptions.ALL_CATEGORIES
            } else {
                repository.update(product)
            }
            onSuccess()
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            repository.update(product.copy(isFavorite = !product.isFavorite))
        }
    }

    /**
     * Adds [change] units (positive = Stock In, negative = Stock Out).
     * Returns false, and changes nothing, if the result would be below zero.
     */
    fun adjustStock(product: Product, change: Int): Boolean {
        val newQuantity = product.quantity + change
        if (newQuantity < 0) return false
        viewModelScope.launch {
            repository.update(product.copy(quantity = newQuantity))
        }
        return true
    }

    fun deleteProduct(product: Product, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.delete(product)
            onDeleted()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StockFlowApplication
                ProductViewModel(app.repository)
            }
        }
    }
}
