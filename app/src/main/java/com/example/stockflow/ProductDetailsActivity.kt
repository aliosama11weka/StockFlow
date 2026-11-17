package com.example.stockflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        db = AppDatabase.getDatabase(this)

        val productId = intent.getIntExtra("PRODUCT_ID", 0)

        val imgProduct = findViewById<ImageView>(R.id.imgProductDetail)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvSku = findViewById<TextView>(R.id.tvDetailSku)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvColor = findViewById<TextView>(R.id.tvDetailColor)
        val tvSize = findViewById<TextView>(R.id.tvDetailSize)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val tvStock = findViewById<TextView>(R.id.tvDetailStock)
        val btnFavorite = findViewById<ImageButton>(R.id.btnDetailFavorite)
        val btnEdit = findViewById<ImageButton>(R.id.btnDetailEdit)
        val btnDelete = findViewById<ImageButton>(R.id.btnDetailDelete)
        val btnShare = findViewById<ImageButton>(R.id.btnDetailShare)
        val btnStockIn = findViewById<Button>(R.id.btnStockIn)
        val btnStockOut = findViewById<Button>(R.id.btnStockOut)
        val btnBack = findViewById<ImageButton>(R.id.btnDetailBack)

        btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            db.appDao().getProductById(productId).collectLatest { product ->
                if (product == null) { finish(); return@collectLatest }
                currentProduct = product

                tvName.text = product.name
                tvSku.text = "SKU: ${product.sku}"
                tvCategory.text = "Category: ${product.category}"
                tvColor.text = "Color: ${product.color}"
                tvSize.text = "Size: ${product.size}"
                tvDescription.text = if (product.description.isNotEmpty()) product.description else "No description"
                tvPrice.text = "EGP ${String.format("%.2f", product.sellingPrice)}"

                when {
                    product.quantity == 0 -> {
                        tvStock.text = "Out of stock"
                        tvStock.setTextColor(0xFFC62828.toInt())
                    }
                    product.quantity <= ProductOptions.LOW_STOCK_LIMIT -> {
                        tvStock.text = "Low stock · ${product.quantity} left"
                        tvStock.setTextColor(0xFFE65100.toInt())
                    }
                    else -> {
                        tvStock.text = "${product.quantity} in stock"
                        tvStock.setTextColor(0xFF2E7D32.toInt())
                    }
                }

                btnFavorite.setImageResource(
                    if (product.isFavorite) android.R.drawable.btn_star_big_on
                    else android.R.drawable.btn_star_big_off
                )

                if (!product.imageUri.isNullOrEmpty()) {
                    imgProduct.load(product.imageUri) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }

        btnFavorite.setOnClickListener {
            currentProduct?.let { p ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.appDao().updateProduct(p.copy(isFavorite = !p.isFavorite))
                }
            }
        }

        btnEdit.setOnClickListener {
            currentProduct?.let { p ->
                val intent = Intent(this, AddEditProductActivity::class.java)
                intent.putExtra("PRODUCT_ID", p.id)
                startActivity(intent)
            }
        }

        btnDelete.setOnClickListener {
            currentProduct?.let { p ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete \"${p.name}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            db.appDao().deleteProduct(p)
                            withContext(Dispatchers.Main) { finish() }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        btnShare.setOnClickListener {
            currentProduct?.let { p ->
                val shareText = """
                    StockFlow Product
                    Name: ${p.name}
                    SKU: ${p.sku}
                    Category: ${p.category} · ${p.color} · Size ${p.size}
                    Stock: ${p.quantity} units
                    Price: EGP ${String.format("%.2f", p.sellingPrice)}
                """.trimIndent()
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, shareText)
                startActivity(Intent.createChooser(intent, "Share product"))
            }
        }

        btnStockIn.setOnClickListener {
            currentProduct?.let { p ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.appDao().updateProduct(p.copy(quantity = p.quantity + 1))
                }
            }
        }

        btnStockOut.setOnClickListener {
            currentProduct?.let { p ->
                if (p.quantity <= 0) {
                    Toast.makeText(this, "Already out of stock", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    db.appDao().updateProduct(p.copy(quantity = p.quantity - 1))
                }
            }
        }
    }
}
