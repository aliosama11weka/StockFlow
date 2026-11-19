package com.stockflow.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Every destination (route) in the app. */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")

    data object ProductDetails : Screen("product/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: Int) = "product/$productId"
    }

    /** Used for both Add (no id) and Edit (with id). */
    data object ProductForm : Screen("product_form?productId={productId}") {
        const val ARG_PRODUCT_ID = "productId"
        const val NEW_PRODUCT = -1
        fun addRoute() = "product_form"
        fun editRoute(productId: Int) = "product_form?productId=$productId"
    }
}

/** The four tabs of the bottom navigation bar. */
data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Filled.Search),
    BottomNavItem(Screen.Favorites, "Favorites", Icons.Filled.Favorite),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings)
)
