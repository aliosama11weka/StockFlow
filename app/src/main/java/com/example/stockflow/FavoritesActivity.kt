package com.example.stockflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        db = AppDatabase.getDatabase(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        adapter = ProductAdapter(
            onItemClick = { product ->
                val intent = Intent(this, ProductDetailsActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onFavoriteClick = { product ->
                lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    db.appDao().updateProduct(product.copy(isFavorite = !product.isFavorite))
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            db.appDao().getFavoriteProducts().collectLatest { products ->
                adapter.submitList(products)
            }
        }

        bottomNav.selectedItemId = R.id.nav_favorites
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); false }
                R.id.nav_search -> { startActivity(Intent(this, SearchActivity::class.java)); false }
                R.id.nav_favorites -> true
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); false }
                else -> false
            }
        }
    }
}
