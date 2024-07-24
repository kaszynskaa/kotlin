package com.s21845.groceryapp // Package declaration

// Required imports for Room database annotations and Date handling
import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date // Import for date-related operations

// Entity declaration for the Room database
@Entity(tableName = "Grocery_items") // Defines the entity with a specified table name
data class GroceryItems ( // Data class to represent a grocery item
    @ColumnInfo(name = "itemName") // Define the column name in the database
    var itemName: String, // Name of the grocery item

    @ColumnInfo(name = "itemQuantity") // Define the column name
    var itemQuantity: Double, // Quantity of the grocery item

    @ColumnInfo(name = "itemPrice") // Define the column name
    var itemPrice: Double, // Price of the grocery item

    @ColumnInfo(name = "expirationDate") // Define the column name
    var expirationDate: Date?, // Expiration date, nullable

    @ColumnInfo(name = "category") // Define the column for item category
    var selectedCategory: String?, // Selected category, default is null

    @ColumnInfo(name = "imageUri") // Define the column for item image URI
    var selectedImageUri: String? // URI for the item's image, default is null
) {
    @DrawableRes // Annotation to indicate that the ID represents a drawable resource
    @PrimaryKey(autoGenerate = true) // Indicates the primary key and auto-generation
    var id: Int? = null // Primary key, nullable and auto-generated

    // Function to determine if the grocery item is expired
    fun isExpired(): Boolean {
        val currentDate = Date() // Get the current date
        return expirationDate?.let { currentDate.after(it) } ?: false // Check if expiration date is in the past
    }
}
