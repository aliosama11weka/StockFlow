package com.stockflow.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One clothing product stored in the Room database.
 * Each property becomes a column in the "products" table.
 */
@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"], unique = true)]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val sku: String,
    val category: String,
    val color: String,
    val size: String,
    val description: String = "",
    val quantity: Int,
    val sellingPrice: Double,
    val isFavorite: Boolean = false,
    val imageUri: String? = null
)

/** Fixed lists used by the filter chips and the product form. */
object ProductOptions {
    const val ALL_CATEGORIES = "All"

    val categories: List<String> = listOf(
        "T-Shirts", "Shirts", "Hoodies", "Pants", "Jackets", "Accessories"
    )

    val sizes: List<String> = listOf("XS", "S", "M", "L", "XL", "XXL", "One Size")

    /** Products at or below this quantity are marked as "Low stock". */
    const val LOW_STOCK_LIMIT = 5

    const val CURRENCY = "EGP"
}
