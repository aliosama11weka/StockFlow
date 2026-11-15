package com.example.stockflow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ProductAdapter
    private var selectedCategory = ProductOptions.ALL_CATEGORIES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        db = AppDatabase.getDatabase(this)

        // Older installs: swap the old web photos for the bundled product images
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) { refreshSeedImages(db.appDao()) }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProducts)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategories)
        val searchInput = findViewById<TextInputEditText>(R.id.editTextSearch)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddProduct)
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

        // Category chips
        val categories = listOf(ProductOptions.ALL_CATEGORIES) + ProductOptions.categories
        categories.forEach { cat ->
            val chip = Chip(this)
            chip.text = cat
            chip.isCheckable = true
            chip.isChecked = cat == ProductOptions.ALL_CATEGORIES
            chip.setChipBackgroundColorResource(R.color.chip_bg)
            chip.setTextColor(getColorStateList(R.color.chip_text))
            chip.isCheckedIconVisible = false
            chip.chipStrokeWidth = 0f
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                selectedCategory = chip?.text?.toString() ?: ProductOptions.ALL_CATEGORIES
                loadProducts(searchInput.text?.toString() ?: "")
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadProducts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }

        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    false
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    false
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }

        loadProducts("")
    }

    override fun onResume() {
        super.onResume()
        loadProducts(
            findViewById<TextInputEditText>(R.id.editTextSearch).text?.toString() ?: ""
        )
    }

    private fun loadProducts(query: String) {
        lifecycleScope.launch {
            if (query.isNotBlank()) {
                db.appDao().searchProducts(query).collectLatest { products ->
                    adapter.submitList(products)
                }
            } else if (selectedCategory == ProductOptions.ALL_CATEGORIES) {
                db.appDao().getAllProducts().collectLatest { products ->
                    adapter.submitList(products)
                }
            } else {
                db.appDao().getProductsByCategory(selectedCategory).collectLatest { products ->
                    adapter.submitList(products)
                }
            }
        }
    }
}
