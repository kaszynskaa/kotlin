package com.s21845.groceryapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GroceryViewModel(private val repository: GroceryRepository) : ViewModel() {
    // Insert a new grocery item with optional category and imageUri
    fun insert(item: GroceryItems, category: String? = null, imageUri: String? = null) {
        val itemWithCategoryAndImage = item.copy(selectedCategory = category, selectedImageUri = imageUri)
        viewModelScope.launch {
            repository.insert(itemWithCategoryAndImage) // Insert the item with the given fields
        }
    }

    // Delete an existing grocery item
    fun delete(item: GroceryItems) {
        viewModelScope.launch {
            repository.delete(item) // Launch coroutine to delete the item
        }
    }

    // Update an existing grocery item with new data
    fun update(groceryItems: GroceryItems) {
        viewModelScope.launch {
            repository.update(groceryItems) // Launch coroutine to update the item
        }
    }

    // Mark an item as thrown away (e.g., when expired)
    fun markAsThrownAway(item: GroceryItems) {
        viewModelScope.launch {
            item.selectedCategory = "Thrown Away" // Update category to "Thrown Away"
            repository.update(item) // Update the item in the database
        }
    }

    // Get all grocery items sorted by expiration date
    fun getAllGroceryItemsSortedByExpirationDate(): LiveData<List<GroceryItems>> {
        return repository.getAllGroceryItemsSortedByExpirationDate() // Fetch from repository
    }

    // Get grocery items by a specific category
    fun getItemsByCategory(category: String): LiveData<List<GroceryItems>> {
        return repository.getItemsByCategory(category) // Fetch by category from repository
    }

    // Get expired grocery items
    fun getExpiredItems(): LiveData<List<GroceryItems>> {
        return repository.getExpiredItems() // Fetch expired items
    }

    // Get valid (non-expired) grocery items
    fun getValidItems(): LiveData<List<GroceryItems>> {
        return repository.getValidItems() // Fetch valid items
    }

    // Get items by category and expired status
    fun getItemsByCategoryAndExpiredStatus(category: String): LiveData<List<GroceryItems>> {
        return repository.getItemsByCategoryAndExpiredStatus(category) // Fetch items by category and expired status
    }

    // Get items by category and valid status
    fun getItemsByCategoryAndValidStatus(category: String): LiveData<List<GroceryItems>> {
        return repository.getItemsByCategoryAndValidStatus(category) // Fetch by category and valid status
    }
}
