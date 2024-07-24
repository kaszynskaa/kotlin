package com.s21845.groceryapp // Package declaration

// Required imports for ViewModel and ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory class for creating instances of the GroceryViewModel
class GroceryViewModelFactory(private val repository: GroceryRepository) : ViewModelProvider.Factory {

    // Override the create method to return the correct ViewModel instance
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is GroceryViewModel or its subclass
        if (modelClass.isAssignableFrom(GroceryViewModel::class.java)) {
            // Suppress unchecked cast warning since we're sure about the class type
            @Suppress("UNCHECKED_CAST")
            return GroceryViewModel(repository) as T // Return an instance of GroceryViewModel with the provided repository
        }
        // Throw an exception if the class is not recognized
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
