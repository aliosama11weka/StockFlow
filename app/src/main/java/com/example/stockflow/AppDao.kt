package com.example.stockflow

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id DESC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY name")
    fun getFavoriteProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Int): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdOnce(id: Int): Product?

    @Query("SELECT COUNT(*) FROM products WHERE sku = :sku AND id != :excludeId")
    suspend fun countProductsWithSku(sku: String, excludeId: Int): Int

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM products")
    fun getTotalUnits(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCountOnce(): Int

    @Query("SELECT COUNT(*) FROM products WHERE quantity = 0")
    fun getOutOfStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE quantity > 0 AND quantity <= :limit")
    fun getLowStockCount(limit: Int): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity * sellingPrice), 0.0) FROM products")
    fun getInventoryValue(): Flow<Double>

    @Query("SELECT * FROM products ORDER BY category, name")
    suspend fun getAllProductsOnce(): List<Product>

    @Query("UPDATE products SET isFavorite = 0")
    suspend fun clearFavorites()

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: Product): Long

    // Only touches rows that still hold the old web photo, so user-chosen images are kept.
    @Query("UPDATE products SET imageUri = :uri WHERE sku = :sku AND imageUri LIKE 'https://images.unsplash.com/%'")
    suspend fun replaceWebSeedImage(sku: String, uri: String)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // ---------- Users ----------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): User?
}
