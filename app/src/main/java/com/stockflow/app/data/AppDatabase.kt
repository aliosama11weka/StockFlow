package com.stockflow.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The Room database. Only one instance is created for the whole app
 * (see [getDatabase]), and it is saved on the device, so data stays
 * after the app is closed and reopened.
 */
@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Return the existing database, or create it once in a thread-safe way.
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockflow_database"
                ).build()
                instance = newInstance
                newInstance
            }
        }
    }
}
