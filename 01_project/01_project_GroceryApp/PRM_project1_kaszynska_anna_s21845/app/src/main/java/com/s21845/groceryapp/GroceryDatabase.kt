package com.s21845.groceryapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.TypeConverters

@Database(entities = [GroceryItems::class], version = 23, exportSchema = false)
@TypeConverters(Converters::class) // Assuming you have custom converters, e.g., for dates
abstract class GroceryDatabase : RoomDatabase() {

    abstract fun getGroceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: GroceryDatabase? = null

        // Migration from version 18 to 19 to add the `imageUri` column
        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Grocery_items ADD COLUMN imageUri TEXT DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): GroceryDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GroceryDatabase::class.java,
                        "grocery_database"
                    )
                        .addMigrations(MIGRATION_18_19) // Add migration logic to handle schema changes
                        .fallbackToDestructiveMigration() // Optional: Fall back to destructive migration
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
