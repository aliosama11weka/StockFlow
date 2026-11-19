package com.example.stockflow

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvSku: TextView = itemView.findViewById(R.id.tvProductSku)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvStock: TextView = itemView.findViewById(R.id.tvStockBadge)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(product: Product) {
            tvName.text = product.name
            tvCategory.text = "${product.category} · ${product.color} · Size ${product.size}"
            tvSku.text = "SKU: ${product.sku}"
            tvPrice.text = "EGP ${String.format("%.2f", product.sellingPrice)}"

            // Stock badge
            when {
                product.quantity == 0 -> {
                    tvStock.text = "Out of stock"
                    tvStock.backgroundTintList = ColorStateList.valueOf(0xFFFFEBEE.toInt())
                    tvStock.setTextColor(0xFFC62828.toInt())
                }
                product.quantity <= ProductOptions.LOW_STOCK_LIMIT -> {
                    tvStock.text = "Low · ${product.quantity} left"
                    tvStock.backgroundTintList = ColorStateList.valueOf(0xFFFFF3E0.toInt())
                    tvStock.setTextColor(0xFFE65100.toInt())
                }
                else -> {
                    tvStock.text = "${product.quantity} in stock"
                    tvStock.backgroundTintList = ColorStateList.valueOf(0xFFE8F5E9.toInt())
                    tvStock.setTextColor(0xFF2E7D32.toInt())
                }
            }

            // Favorite icon
            btnFavorite.setImageResource(
                if (product.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )

            // Load image with Coil
            if (!product.imageUri.isNullOrEmpty()) {
                imgProduct.load(product.imageUri) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                }
            } else {
                imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener { onItemClick(product) }
            btnFavorite.setOnClickListener { onFavoriteClick(product) }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
