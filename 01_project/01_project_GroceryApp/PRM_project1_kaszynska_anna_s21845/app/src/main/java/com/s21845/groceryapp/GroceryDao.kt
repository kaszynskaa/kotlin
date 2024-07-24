package com.s21845.groceryapp

import androidx.lifecycle.LiveData
import androidx.room.*
import com.s21845.groceryapp.GroceryItems
import java.util.Date

@Dao
interface GroceryDao {
    // Insert a new grocery item, with conflict strategy to replace if the item exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: GroceryItems)

    // Delete an existing grocery item
    @Delete
    fun delete(item: GroceryItems)

    // Update an existing grocery item, using suspend for coroutine support
    @Update
    suspend fun update(groceryItems: GroceryItems)

    // Query to fetch all grocery items sorted by expiration date
    @Query("SELECT * FROM Grocery_items ORDER BY expirationDate ASC")
    fun getAllGroceryItemsSortedByExpirationDate(): LiveData<List<GroceryItems>>

    // Query to fetch grocery items by category, sorted by expiration date
    @Query("SELECT * FROM Grocery_items WHERE category = :category ORDER BY expirationDate ASC")
    fun getItemsByCategory(category: String): LiveData<List<GroceryItems>>

    // Query to fetch expired grocery items, given a specific current date
    @Query("SELECT * FROM Grocery_items WHERE expirationDate <= :currentDate ORDER BY expirationDate ASC")
    fun getExpiredItems(currentDate: Long): LiveData<List<GroceryItems>>

    // Query to fetch valid grocery items (not expired), given a specific current date
    @Query("SELECT * FROM Grocery_items WHERE expirationDate > :currentDate ORDER BY expirationDate ASC")
    fun getValidItems(currentDate: Long): LiveData<List<GroceryItems>>

    // Query to fetch grocery items by category and expired status
    @Query("SELECT * FROM Grocery_items WHERE category = :category AND expirationDate <= :currentDate ORDER BY expirationDate ASC")
    fun getItemsByCategoryAndExpiredStatus(category: String, currentDate: Long): LiveData<List<GroceryItems>>

    // Query to fetch grocery items by category and valid status (not expired)
    @Query("SELECT * FROM Grocery_items WHERE category = :category AND expirationDate > :currentDate ORDER BY expirationDate ASC")
    fun getItemsByCategoryAndValidStatus(category: String, currentDate: Long): LiveData<List<GroceryItems>>

    // Query to fetch grocery items that have associated images (imageUri is not null)
    @Query("SELECT * FROM Grocery_items WHERE imageUri IS NOT NULL")
    fun getItemsWithImages(): LiveData<List<GroceryItems>>
}
