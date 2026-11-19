package com.stockflow.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stockflow.app.ui.screens.FavoritesScreen
import com.stockflow.app.ui.screens.HomeScreen
import com.stockflow.app.ui.screens.ProductDetailsScreen
import com.stockflow.app.ui.screens.ProductFormScreen
import com.stockflow.app.ui.screens.SearchScreen
import com.stockflow.app.ui.screens.SettingsScreen
import com.stockflow.app.viewmodel.ProductViewModel
import com.stockflow.app.viewmodel.SettingsViewModel

/**
 * Connects every screen to a route. The four tab screens receive the
 * bottom bar; Add/Edit and Details are opened on top of them.
 */
@Composable
fun StockFlowNavHost(
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    // One shared ViewModel (scoped to the Activity) so all screens see the same data.
    productViewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory)
) {
    val bottomBar: @Composable () -> Unit = { BottomNavBar(navController) }
    val openProduct: (Int) -> Unit = { id -> navController.navigate(Screen.ProductDetails.createRoute(id)) }

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = productViewModel,
                bottomBar = bottomBar,
                onProductClick = openProduct,
                onAddProduct = { navController.navigate(Screen.ProductForm.addRoute()) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(viewModel = productViewModel, bottomBar = bottomBar, onProductClick = openProduct)
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(viewModel = productViewModel, bottomBar = bottomBar, onProductClick = openProduct)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = settingsViewModel, bottomBar = bottomBar)
        }

        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(navArgument(Screen.ProductDetails.ARG_PRODUCT_ID) { type = NavType.IntType })
        ) { entry ->
            val productId = entry.arguments?.getInt(Screen.ProductDetails.ARG_PRODUCT_ID) ?: 0
            ProductDetailsScreen(
                productId = productId,
                viewModel = productViewModel,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.ProductForm.editRoute(id)) }
            )
        }

        composable(
            route = Screen.ProductForm.route,
            arguments = listOf(
                navArgument(Screen.ProductForm.ARG_PRODUCT_ID) {
                    type = NavType.IntType
                    defaultValue = Screen.ProductForm.NEW_PRODUCT
                }
            )
        ) { entry ->
            val productId = entry.arguments?.getInt(Screen.ProductForm.ARG_PRODUCT_ID)
                ?: Screen.ProductForm.NEW_PRODUCT
            ProductFormScreen(
                productId = productId,
                viewModel = productViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { isNewProduct ->
                    if (isNewProduct) {
                        // Go back to the Home list, where the new product appears first.
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    } else {
                        // Return to the details screen, which already shows the changes.
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
