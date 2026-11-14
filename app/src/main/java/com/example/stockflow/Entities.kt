package com.example.stockflow

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"], unique = true)]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val password: String
)

object ProductOptions {
    const val ALL_CATEGORIES = "All"
    val categories = listOf("T-Shirts", "Shirts", "Hoodies", "Pants", "Jackets", "Accessories")
    val sizes = listOf("XS", "S", "M", "L", "XL", "XXL", "One Size")
    const val LOW_STOCK_LIMIT = 5
    const val CURRENCY = "EGP"
}
