package com.stockflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stockflow.app.StockFlowApplication
import com.stockflow.app.data.AppTheme
import com.stockflow.app.data.ThemePreferences
import com.stockflow.app.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Numbers read live from Room for the Settings screen. */
data class DatabaseStats(
    val totalProducts: Int = 0,
    val favoriteProducts: Int = 0,
    val totalUnits: Int = 0
)

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    repository: ProductRepository
) : ViewModel() {

    val theme: StateFlow<AppTheme> = themePreferences.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.SYSTEM)

    val stats: StateFlow<DatabaseStats> = combine(
        repository.productCount,
        repository.favoriteCount,
        repository.totalUnits
    ) { products, favorites, units ->
        DatabaseStats(totalProducts = products, favoriteProducts = favorites, totalUnits = units)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DatabaseStats())

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { themePreferences.setTheme(theme) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StockFlowApplication
                SettingsViewModel(app.themePreferences, app.repository)
            }
        }
    }
}
