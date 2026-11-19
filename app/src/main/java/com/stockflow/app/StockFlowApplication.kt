package com.stockflow.app

import android.app.Application
import com.stockflow.app.data.AppDatabase
import com.stockflow.app.data.ThemePreferences
import com.stockflow.app.repository.ProductRepository

/**
 * Created once when the app starts. It owns the database, the repository
 * and the theme preferences so every ViewModel shares the same objects.
 */
class StockFlowApplication : Application() {

    // "by lazy" creates each object the first time it is needed.
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: ProductRepository by lazy { ProductRepository(database.productDao()) }
    val themePreferences: ThemePreferences by lazy { ThemePreferences(this) }
}
