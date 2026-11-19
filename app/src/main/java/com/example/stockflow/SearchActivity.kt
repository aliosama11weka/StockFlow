package com.example.stockflow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        db = AppDatabase.getDatabase(this)

        val searchInput = findViewById<TextInputEditText>(R.id.editTextSearchQuery)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSearch)
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

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.appDao().searchProducts(query).collectLatest { products ->
                            adapter.submitList(products)
                        }
                    }
                } else {
                    adapter.submitList(emptyList())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomNav.selectedItemId = R.id.nav_search
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); false }
                R.id.nav_search -> true
                R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); false }
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); false }
                else -> false
            }
        }
    }
}
