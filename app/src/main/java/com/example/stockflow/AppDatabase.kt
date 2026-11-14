package com.example.stockflow

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Product::class, User::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockflow_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

suspend fun seedDatabase(dao: AppDao) {
    // T-Shirts
    dao.insertProduct(Product(
        name = "Classic White Tee", sku = "TS-001", category = "T-Shirts",
        color = "White", size = "M", quantity = 25, sellingPrice = 149.99,
        description = "Essential white crew-neck t-shirt, 100% cotton.",
        imageUri = seedImageUri("TS-001")
    ))
    dao.insertProduct(Product(
        name = "Black Graphic Tee", sku = "TS-002", category = "T-Shirts",
        color = "Black", size = "L", quantity = 18, sellingPrice = 199.99,
        description = "Bold graphic print on a slim-fit black tee.",
        imageUri = seedImageUri("TS-002")
    ))
    dao.insertProduct(Product(
        name = "Navy Striped Tee", sku = "TS-003", category = "T-Shirts",
        color = "Navy", size = "S", quantity = 3, sellingPrice = 179.99,
        description = "French-inspired navy and white horizontal stripes.",
        imageUri = seedImageUri("TS-003")
    ))

    // Shirts
    dao.insertProduct(Product(
        name = "Oxford Blue Shirt", sku = "SH-001", category = "Shirts",
        color = "Blue", size = "M", quantity = 12, sellingPrice = 349.99,
        description = "Classic Oxford button-down shirt in light blue.",
        imageUri = seedImageUri("SH-001")
    ))
    dao.insertProduct(Product(
        name = "White Formal Shirt", sku = "SH-002", category = "Shirts",
        color = "White", size = "L", quantity = 8, sellingPrice = 299.99,
        description = "Crisp white formal shirt for office wear.",
        imageUri = seedImageUri("SH-002")
    ))
    dao.insertProduct(Product(
        name = "Flannel Check Shirt", sku = "SH-003", category = "Shirts",
        color = "Red/Black", size = "XL", quantity = 5, sellingPrice = 379.99,
        description = "Cozy flannel shirt with classic check pattern.",
        imageUri = seedImageUri("SH-003")
    ))

    // Hoodies
    dao.insertProduct(Product(
        name = "Grey Pullover Hoodie", sku = "HD-001", category = "Hoodies",
        color = "Grey", size = "M", quantity = 20, sellingPrice = 499.99,
        description = "Soft fleece pullover hoodie with kangaroo pocket.",
        imageUri = seedImageUri("HD-001")
    ))
    dao.insertProduct(Product(
        name = "Black Zip Hoodie", sku = "HD-002", category = "Hoodies",
        color = "Black", size = "L", quantity = 4, sellingPrice = 549.99,
        description = "Full-zip hoodie with ribbed cuffs and hem.",
        imageUri = seedImageUri("HD-002")
    ))
    dao.insertProduct(Product(
        name = "Navy College Hoodie", sku = "HD-003", category = "Hoodies",
        color = "Navy", size = "XL", quantity = 15, sellingPrice = 599.99,
        description = "Heavyweight college hoodie with embroidered logo.",
        imageUri = seedImageUri("HD-003")
    ))

    // Pants
    dao.insertProduct(Product(
        name = "Classic Blue Jeans", sku = "PT-001", category = "Pants",
        color = "Blue", size = "32", quantity = 30, sellingPrice = 549.99,
        description = "Straight-fit blue denim jeans with 5-pocket design.",
        imageUri = seedImageUri("PT-001")
    ))
    dao.insertProduct(Product(
        name = "Khaki Chinos", sku = "PT-002", category = "Pants",
        color = "Khaki", size = "34", quantity = 2, sellingPrice = 449.99,
        description = "Smart casual chino pants in khaki cotton twill.",
        imageUri = seedImageUri("PT-002")
    ))
    dao.insertProduct(Product(
        name = "Black Slim Trousers", sku = "PT-003", category = "Pants",
        color = "Black", size = "30", quantity = 0, sellingPrice = 499.99,
        description = "Slim-fit dress trousers for formal occasions.",
        imageUri = seedImageUri("PT-003")
    ))

    // Jackets
    dao.insertProduct(Product(
        name = "Leather Biker Jacket", sku = "JK-001", category = "Jackets",
        color = "Black", size = "M", quantity = 7, sellingPrice = 1299.99,
        description = "Genuine leather biker jacket with zip details.",
        imageUri = seedImageUri("JK-001")
    ))
    dao.insertProduct(Product(
        name = "Denim Jacket", sku = "JK-002", category = "Jackets",
        color = "Blue", size = "L", quantity = 11, sellingPrice = 699.99,
        description = "Classic washed denim jacket with chest pockets.",
        imageUri = seedImageUri("JK-002")
    ))
    dao.insertProduct(Product(
        name = "Puffer Winter Jacket", sku = "JK-003", category = "Jackets",
        color = "Olive", size = "XL", quantity = 3, sellingPrice = 999.99,
        description = "Warm puffer jacket with water-resistant shell.",
        imageUri = seedImageUri("JK-003")
    ))

    // Accessories
    dao.insertProduct(Product(
        name = "Leather Belt", sku = "AC-001", category = "Accessories",
        color = "Brown", size = "One Size", quantity = 40, sellingPrice = 199.99,
        description = "Full-grain leather belt with silver buckle.",
        imageUri = seedImageUri("AC-001")
    ))
    dao.insertProduct(Product(
        name = "Wool Scarf", sku = "AC-002", category = "Accessories",
        color = "Grey", size = "One Size", quantity = 22, sellingPrice = 149.99,
        description = "Soft merino wool scarf in a herringbone pattern.",
        imageUri = seedImageUri("AC-002")
    ))
    dao.insertProduct(Product(
        name = "Classic Cap", sku = "AC-003", category = "Accessories",
        color = "Black", size = "One Size", quantity = 0, sellingPrice = 129.99,
        description = "Adjustable baseball cap with embroidered logo.",
        imageUri = seedImageUri("AC-003")
    ))
}

// ---------------------------------------------------------------------------
// Default product images
// Each default product uses a bundled illustration (res/drawable/img_<sku>.xml)
// that matches its name, colour and description, so it works offline and never
// breaks like a hot-linked photo can.
// ---------------------------------------------------------------------------
private const val SEED_IMAGE_BASE = "android.resource://com.example.stockflow/drawable/"

fun seedImageUri(sku: String): String =
    SEED_IMAGE_BASE + "img_" + sku.lowercase().replace('-', '_')

val seedSkus = listOf(
    "TS-001",
    "TS-002",
    "TS-003",
    "SH-001",
    "SH-002",
    "SH-003",
    "HD-001",
    "HD-002",
    "HD-003",
    "PT-001",
    "PT-002",
    "PT-003",
    "JK-001",
    "JK-002",
    "JK-003",
    "AC-001",
    "AC-002",
    "AC-003"
)

/** Moves databases created by an older build from the web photos to the bundled images. */
suspend fun refreshSeedImages(dao: AppDao) {
    seedSkus.forEach { sku -> dao.replaceWebSeedImage(sku, seedImageUri(sku)) }
}
