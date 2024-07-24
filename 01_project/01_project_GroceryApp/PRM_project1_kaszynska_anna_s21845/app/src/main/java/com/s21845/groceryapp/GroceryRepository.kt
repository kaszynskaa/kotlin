package com.s21845.groceryapp

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class GroceryRepository(private val db: GroceryDatabase) {

    // Insert a new grocery item into the database asynchronously
    suspend fun insert(item: GroceryItems) {
        withContext(Dispatchers.IO) {
            db.getGroceryDao().insert(item) // Insert item on I/O thread
        }
    }

    // Delete an existing grocery item
    suspend fun delete(item: GroceryItems) {
        withContext(Dispatchers.IO) {
            db.getGroceryDao().delete(item) // Delete on I/O thread
        }
    }

    // Update a grocery item in the database
    suspend fun update(groceryItems: GroceryItems) {
        withContext(Dispatchers.IO) {
            db.getGroceryDao().update(groceryItems) // Update on I/O thread
        }
    }

    // Get all grocery items sorted by expiration date
    fun getAllGroceryItemsSortedByExpirationDate(): LiveData<List<GroceryItems>> {
        return db.getGroceryDao().getAllGroceryItemsSortedByExpirationDate() // Return LiveData
    }

    // Get grocery items by specific category
    fun getItemsByCategory(category: String): LiveData<List<GroceryItems>> {
        return db.getGroceryDao().getItemsByCategory(category) // Return LiveData
    }

    // Get grocery items that are expired
    fun getExpiredItems(): LiveData<List<GroceryItems>> {
        val currentDate = Date().time // Current timestamp
        return db.getGroceryDao().getExpiredItems(currentDate) // Return LiveData of expired items
    }

    // Get grocery items that are still valid (not expired)
    fun getValidItems(): LiveData<List<GroceryItems>> {
        val currentDate = Date().time // Current timestamp
        return db.getGroceryDao().getValidItems(currentDate) // Return LiveData of valid items
    }

    // Get grocery items by category and expired status
    fun getItemsByCategoryAndExpiredStatus(category: String): LiveData<List<GroceryItems>> {
        val currentDate = Date().time
        return db.getGroceryDao().getItemsByCategoryAndExpiredStatus(category, currentDate)
    }

    // Get grocery items by category and valid status (not expired)
    fun getItemsByCategoryAndValidStatus(category: String): LiveData<List<GroceryItems>> {
        val currentDate = Date().time
        return db.getGroceryDao().getItemsByCategoryAndValidStatus(category, currentDate)
    }
}
