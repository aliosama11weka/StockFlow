package com.stockflow.app.repository

import com.stockflow.app.data.Product
import com.stockflow.app.data.ProductDao
import com.stockflow.app.data.ProductOptions
import kotlinx.coroutines.flow.Flow

/**
 * A single place the ViewModels use to reach the database.
 * It keeps database details out of the UI code.
 */
class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val favoriteProducts: Flow<List<Product>> = productDao.getFavoriteProducts()
    val productCount: Flow<Int> = productDao.getProductCount()
    val favoriteCount: Flow<Int> = productDao.getFavoriteCount()
    val totalUnits: Flow<Int> = productDao.getTotalUnits()

    fun productsInCategory(category: String): Flow<List<Product>> =
        if (category == ProductOptions.ALL_CATEGORIES) allProducts
        else productDao.getProductsByCategory(category)

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    fun getProduct(id: Int): Flow<Product?> = productDao.getProductById(id)

    suspend fun getProductOnce(id: Int): Product? = productDao.getProductByIdOnce(id)

    suspend fun isSkuTaken(sku: String, excludeId: Int): Boolean =
        productDao.countProductsWithSku(sku, excludeId) > 0

    suspend fun insert(product: Product): Long = productDao.insertProduct(product)

    suspend fun update(product: Product) = productDao.updateProduct(product)

    suspend fun delete(product: Product) = productDao.deleteProduct(product)
}
