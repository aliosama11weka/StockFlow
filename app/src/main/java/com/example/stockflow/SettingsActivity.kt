package com.example.stockflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var dao: AppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        dao = AppDatabase.getDatabase(this).appDao()
        val session = SessionManager(this)

        findViewById<TextView>(R.id.tvLoggedInAs).text =
            "Logged in as: ${session.getUserEmail() ?: "Unknown"}"
        findViewById<TextView>(R.id.tvLowStockCaption).text =
            "Items with ${ProductOptions.LOW_STOCK_LIMIT} or fewer units left"

        // Live numbers for the overview card
        bind(dao.getProductCount(), R.id.tvTotalProducts) { it.toString() }
        bind(dao.getFavoriteCount(), R.id.tvFavoriteProducts) { it.toString() }
        bind(dao.getTotalUnits(), R.id.tvTotalUnits) { it.toString() }
        bind(dao.getLowStockCount(ProductOptions.LOW_STOCK_LIMIT), R.id.tvLowStock) { it.toString() }
        bind(dao.getOutOfStockCount(), R.id.tvOutOfStock) { it.toString() }
        bind(dao.getInventoryValue(), R.id.tvInventoryValue) {
            "${ProductOptions.CURRENCY} ${String.format("%,.0f", it)}"
        }

        findViewById<View>(R.id.rowShareReport).setOnClickListener { shareReport() }

        findViewById<View>(R.id.rowClearFavorites).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dao.clearFavorites()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Favorites cleared", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<View>(R.id.rowRestoreDefaults).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restore default products")
                .setMessage("This deletes every product (including ones you added) and re-adds the 18 starter products. Continue?")
                .setPositiveButton("Restore") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        dao.deleteAllProducts()
                        seedDatabase(dao)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SettingsActivity, "Default products restored", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            session.logout()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_settings
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); false }
                R.id.nav_search -> { startActivity(Intent(this, SearchActivity::class.java)); false }
                R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); false }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun <T> bind(flow: Flow<T>, viewId: Int, format: (T) -> String) {
        val view = findViewById<TextView>(viewId)
        lifecycleScope.launch { flow.collect { view.text = format(it) } }
    }

    private fun shareReport() {
        lifecycleScope.launch(Dispatchers.IO) {
            val products = dao.getAllProductsOnce()
            val limit = ProductOptions.LOW_STOCK_LIMIT
            val out = products.filter { it.quantity == 0 }
            val low = products.filter { it.quantity in 1..limit }
            val units = products.sumOf { it.quantity }
            val value = products.sumOf { it.quantity * it.sellingPrice }
            val report = buildString {
                appendLine("StockFlow inventory report")
                appendLine("Products: ${products.size}  |  Units: $units")
                appendLine("Inventory value: ${ProductOptions.CURRENCY} ${String.format("%,.2f", value)}")
                appendLine()
                appendLine("Out of stock (${out.size}):")
                if (out.isEmpty()) appendLine("- none") else out.forEach { appendLine("- ${it.name} (${it.sku})") }
                appendLine()
                appendLine("Low stock (${low.size}):")
                if (low.isEmpty()) appendLine("- none") else low.forEach { appendLine("- ${it.name} (${it.sku}): ${it.quantity} left") }
            }
            withContext(Dispatchers.Main) {
                val send = Intent(Intent.ACTION_SEND)
                send.type = "text/plain"
                send.putExtra(Intent.EXTRA_TEXT, report)
                startActivity(Intent.createChooser(send, "Share inventory report"))
            }
        }
    }
}
