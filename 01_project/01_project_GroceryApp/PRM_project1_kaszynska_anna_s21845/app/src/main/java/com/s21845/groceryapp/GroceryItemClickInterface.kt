package com.s21845.groceryapp // Package declaration

// Interface definition for handling grocery item interactions
interface GroceryItemClickInterface {
    // Method to handle clicking on a grocery item
    fun onItemClick(groceryItems: GroceryItems)

    // Method to mark a grocery item as thrown away
    fun markAsThrownAway(groceryItems: GroceryItems)

    // Method to delete a grocery item
    fun onDeleteClick(currentItem: GroceryItems)
}
