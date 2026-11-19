package com.stockflow.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object: every database operation the app performs.
 * Functions that return Flow are observed by the UI, so the screens
 * update automatically whenever the table changes.
 */
@Dao
interface ProductDao {

    // ---------- Create ----------
    @Insert
    suspend fun insertProduct(product: Product): Long

    // ---------- Read ----------
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id DESC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Int): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdOnce(id: Int): Product?

    @Query(
        "SELECT * FROM products " +
            "WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' " +
            "ORDER BY name"
    )
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY name")
    fun getFavoriteProducts(): Flow<List<Product>>

    /** Used to keep every SKU unique. [excludeId] skips the product being edited. */
    @Query("SELECT COUNT(*) FROM products WHERE sku = :sku COLLATE NOCASE AND id != :excludeId")
    suspend fun countProductsWithSku(sku: String, excludeId: Int): Int

    // ---------- Statistics for the Settings screen ----------
    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM products")
    fun getTotalUnits(): Flow<Int>

    // ---------- Update ----------
    @Update
    suspend fun updateProduct(product: Product)

    // ---------- Delete ----------
    @Delete
    suspend fun deleteProduct(product: Product)
}
