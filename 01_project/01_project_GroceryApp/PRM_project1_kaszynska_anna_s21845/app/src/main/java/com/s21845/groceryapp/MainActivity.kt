package com.s21845.groceryapp

import android.Manifest
import android.content.pm.PackageManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity(), GroceryItemClickInterface {
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 100 // Request code for storage permission
    private lateinit var itemRV: RecyclerView
    private lateinit var addFAB: FloatingActionButton
    private lateinit var groceryRVAdapter: GroceryRVAdapter
    private lateinit var groceryViewModel: GroceryViewModel
    private lateinit var itemCountTextView: TextView
    private lateinit var categorySpinner: Spinner
    private lateinit var expirationStatusRadioGroup: RadioGroup
    private lateinit var filterApplyButton: Button
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check storage permissions and request if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
        }

        // Register the gallery launcher for image selection
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
                Log.d("MainActivity", "Image selected: $selectedImageUri")
            } else {
                Log.e("MainActivity", "Failed to select image")
            }
        }

        // Initialize UI components
        itemRV = findViewById(R.id.rvitems)
        addFAB = findViewById(R.id.fabAdd)
        itemCountTextView = findViewById(R.id.itemCountTextView)
        categorySpinner = findViewById(R.id.categorySpinner)
        expirationStatusRadioGroup = findViewById(R.id.expirationStatusRadioGroup)
        filterApplyButton = findViewById(R.id.filterApplyButton)

        // Set up RecyclerView with adapter and layout manager
        groceryRVAdapter = GroceryRVAdapter(emptyList(), this)
        itemRV.layoutManager = LinearLayoutManager(this)
        itemRV.adapter = groceryRVAdapter

        // Set up ViewModel with repository
        val groceryRepository = GroceryRepository(GroceryDatabase.getInstance(this))
        val factory = GroceryViewModelFactory(groceryRepository)
        groceryViewModel = ViewModelProvider(this, factory).get(GroceryViewModel::class.java)

        // Set up spinner for category selection
        setupCategorySpinner()

        // Set up radio group for expiration status selection
        setupExpirationStatusRadioGroup()

        // Apply filters based on expiration status and category
        applyFilters(false) // Default to valid items

        // Add new item functionality
        addFAB.setOnClickListener {
            openAddItemDialog() // Open dialog to add a new item
        }

        // Observe changes in grocery items and update RecyclerView
        groceryViewModel.getAllGroceryItemsSortedByExpirationDate().observe(this, Observer { itemList ->
            groceryRVAdapter.updateList(itemList)
            updateItemCount(itemList.size) // Update the item count text view
        })

        filterApplyButton.setOnClickListener {
            val isExpired = when (expirationStatusRadioGroup.checkedRadioButtonId) {
                R.id.expirationStatusExpired -> true
                else -> false // Default to valid items
            }
            applyFilters(isExpired) // Apply filters based on expiration status
        }
    }
    // Open dialog to add new grocery items
    private fun openAddItemDialog() {
        val dialog = Dialog(this) // Create new dialog
        dialog.setContentView(R.layout.grocery_add_dialog) // Custom layout for adding new items

        // Find views for various components within the dialog
        val cancelBtn = dialog.findViewById<Button>(R.id.idbtncancel)
        val addBtn = dialog.findViewById<Button>(R.id.idbtnadd)
        val itemNameEdt = dialog.findViewById<EditText>(R.id.idEdtitemname)
        val itemPriceEdt = dialog.findViewById<EditText>(R.id.idEdtitemprice)
        val itemQuantityEdt = dialog.findViewById<EditText>(R.id.idEdtitemquantity)
        val expirationDateEdt = dialog.findViewById<EditText>(R.id.idEdtexpirationdate)
        val categoryRadioGroup = dialog.findViewById<RadioGroup>(R.id.idRadioGroup)

        // Button to select image from the gallery
        val selectImageButton = dialog.findViewById<Button>(R.id.selectImageButton)
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*" // Intent to open gallery
            galleryLauncher.launch(intent) // Launch the gallery
        }
        // Add new item when "Add" button is clicked
        addBtn.setOnClickListener {
            val itemName = itemNameEdt.text.toString().trim()
            if (itemName.isEmpty()) { // Check if the name is empty
                Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit if the name is invalid
            }
            val itemPrice = itemPriceEdt.text.toString().trim().toDoubleOrNull() ?: 0.0
            val itemQuantity = itemQuantityEdt.text.toString().trim().toDoubleOrNull() ?: 1.0
            val expirationDateText = expirationDateEdt.text.toString()

            // Get selected category from RadioGroup
            val selectedCategoryId = categoryRadioGroup.checkedRadioButtonId
            val selectedCategory = when (selectedCategoryId) {
                R.id.idRadioFood -> "Food"
                R.id.idRadioMedicine -> "Medicine"
                R.id.idRadioCosmetics -> "Cosmetics"
                else -> ""
            }
            // Attempt to parse the expiration date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expirationDate: Date? = try {
                dateFormat.parse(expirationDateText) // Parse the expiration date
            } catch (e: Exception) {
                null // Handle parsing exceptions
            }
            val currentDate = Date() // Get current date
            if (expirationDate == null || expirationDate.before(currentDate)) { // Check if the date is in the past
                Toast.makeText(this, "Expiration date cannot be in the past", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit if the date is invalid
            }
            // Create a new GroceryItems object
            val newItem = GroceryItems(
                itemName,
                itemQuantity,
                itemPrice,
                expirationDate,
                selectedCategory,
                selectedImageUri?.toString() // Set the image URI
            )

            groceryViewModel.insert(newItem,selectedCategory) // Insert the new item
            Toast.makeText(applicationContext, "Item Added", Toast.LENGTH_SHORT).show()
            dialog.dismiss() // Close the dialog
        }

        // Cancel dialog when "Cancel" button is clicked
        cancelBtn.setOnClickListener {
            dialog.dismiss() // Cancel the dialog without action
        }

        // Set click listener to show a DatePicker for selecting expiration date
        expirationDateEdt.setOnClickListener {
            showDatePickerDialog(expirationDateEdt) // Show DatePicker to select date
        }

        dialog.show() // Show the dialog for adding a new item
    }

    // Method to show DatePickerDialog for selecting expiration date
    private fun showDatePickerDialog(targetEditText: EditText) {
        val calendar = Calendar.getInstance() // Get current date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        // Create DatePickerDialog with the current date
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay" // Format the selected date
                targetEditText.setText(selectedDate) // Set the selected date in EditText
            },
            year,
            month,
            day
        )

        datePickerDialog.show() // Show the DatePicker dialog
    }

    // Method to open the dialog for editing an item
    private fun openEditDialog(groceryItems: GroceryItems) {
        val dialog = Dialog(this) // Create new dialog
        dialog.setContentView(R.layout.grocery_edit_dialog) // Custom layout for editing items

        // Find various views in the dialog
        val itemNameEdt = dialog.findViewById<EditText>(R.id.idEdtitemname)
        val itemPriceEdt = dialog.findViewById<EditText>(R.id.idEdtitemprice)
        val itemQuantityEdt = dialog.findViewById<EditText>(R.id.idEdtitemquantity)
        val expirationDateEdt = dialog.findViewById<EditText>(R.id.idEdtexpirationdate)
        val categoryRadioGroup = dialog.findViewById<RadioGroup>(R.id.idRadioGroup)

        // Button to select image from the gallery
        val selectImageButton = dialog.findViewById<Button>(R.id.selectImageButton)
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK) // Intent to pick an image from the gallery
            intent.type = "image/*" // Set type to image
            galleryLauncher.launch(intent) // Launch gallery to select image
        }

        val saveBtn = dialog.findViewById<Button>(R.id.idbtnsave) // Button to save changes
        val cancelBtn = dialog.findViewById<Button>(R.id.idbtncancel) // Button to cancel changes

        // Populate views with item data
        itemNameEdt.setText(groceryItems.itemName) // Set item name
        itemPriceEdt.setText(groceryItems.itemPrice.toString()) // Set item price
        itemQuantityEdt.setText(groceryItems.itemQuantity.toString()) // Set item quantity
        expirationDateEdt.setText(
            groceryItems.expirationDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) }
        ) // Format expiration date

        // Show DatePicker when expirationDateEdt is clicked
        expirationDateEdt.setOnClickListener {
            showDatePickerDialog(expirationDateEdt) // Show the date picker dialog
        }

        // Check if the name is empty
        if (itemNameEdt.text.isNullOrBlank()) {
            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Set click listener for save button to update item information
        saveBtn.setOnClickListener {
            val itemName = itemNameEdt.text.toString().trim() // Get item name
            if (itemName.isEmpty()) { // Check if the item name is empty
                Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit if the name is invalid
            }
            val itemPrice = itemPriceEdt.text.toString().toDoubleOrNull() ?: 0.0
            val itemQuantity = itemQuantityEdt.text.toString().toDoubleOrNull() ?: 1.0
            val expirationDateString = expirationDateEdt.text.toString()
            // Get selected category from RadioGroup
            val selectedCategoryId = categoryRadioGroup.checkedRadioButtonId
            val selectedCategory = when (selectedCategoryId) {
                R.id.idRadioFood -> "Food"
                R.id.idRadioMedicine -> "Medicine"
                R.id.idRadioCosmetics -> "Cosmetics"
                else -> "unknown"
            }

            // Parse the expiration date and check for errors
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expirationDate: Date? = try {
                dateFormat.parse(expirationDateString) // Parse expiration date
            } catch (e: Exception) {
                null // Handle parsing errors
            }

            val currentDate = Date()
            if (expirationDate == null || expirationDate.before(currentDate)) { // Check if the date is in the past
                Toast.makeText(this, "Expiration date cannot be in the past", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit if the date is invalid
            }

            // Create updated grocery item object with the new values
            val updatedItem = GroceryItems(
                itemName,
                itemQuantity,
                itemPrice,
                expirationDate,
                selectedCategory, // Keep the existing category
                groceryItems.selectedImageUri // Keep the existing image URI
            )

            updatedItem.id = groceryItems.id // Preserve the item ID
            groceryViewModel.update(updatedItem) // Update the item in the ViewModel

            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show() // Show success message
            dialog.dismiss() // Close the dialog
        }

        // Set click listener for cancel button to close the dialog without changes
        cancelBtn.setOnClickListener {
            dialog.dismiss() // Close the dialog without changes
        }

        dialog.show() // Show the edit dialog
    }

    // Handle click on RecyclerView items for edit or delete options
    override fun onItemClick(groceryItems: GroceryItems) {
        // Example click handling: open the edit dialog
        val options = arrayOf("Edit", "Delete")
        val dialogBuilder = AlertDialog.Builder(this) // Create a dialog builder
        dialogBuilder.setTitle("Choose Action") // Set the title
        dialogBuilder.setItems(options) { dialog, which -> // Set the available options
            when (which) {
                0 -> {
                    if (groceryItems.isExpired()) { // If item is expired
                        Toast.makeText(this, "Cannot edit expired item", Toast.LENGTH_SHORT).show() // Display message
                    } else {
                        openEditDialog(groceryItems) // Open the edit dialog
                    }
                }
                1 -> {
                    groceryViewModel.delete(groceryItems) // Delete the item from the ViewModel
                    Toast.makeText(applicationContext, "Item Deleted Successfully.", Toast.LENGTH_SHORT).show() // Show success message
                }
            }
        }
        dialogBuilder.show() // Show the alert dialog
    }

    // Method to mark a grocery item as thrown away
    override fun markAsThrownAway(groceryItems: GroceryItems) {
        // Mark the item as thrown away in the ViewModel
        groceryViewModel.markAsThrownAway(groceryItems)
    }

    // Set up the category spinner to apply filters based on category
    private fun setupCategorySpinner() {
        // Set up the spinner for category selection
        val categories = arrayOf("All", "Food", "Medicine", "Cosmetics") // Define available categories
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories) // Create array adapter
        categorySpinner.adapter = adapter // Set the adapter for the spinner

        categorySpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters(false) // Apply filters based on category selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                applyFilters(false) // Default to valid items
            }
        })
    }

    // Apply filters based on the selected category and expiration status
    private fun applyFilters(isExpired: Boolean) {
        val selectedCategory = categorySpinner.selectedItem as String
        if (selectedCategory == "All") {
            if (isExpired) {
                groceryViewModel.getExpiredItems().observe(this, Observer { itemList ->
                    groceryRVAdapter.updateList(itemList) // Update the RecyclerView
                })
            } else {
                groceryViewModel.getValidItems().observe(this, Observer { itemList ->
                    groceryRVAdapter.updateList(itemList) // Update the RecyclerView
                })
            }
        } else {
            if (isExpired) { // If a specific category is selected and expired status is chosen
                groceryViewModel.getItemsByCategoryAndExpiredStatus(selectedCategory).observe(this, Observer { itemList ->
                    groceryRVAdapter.updateList(itemList)
                })
            } else { // If a specific category is selected and valid status is chosen
                groceryViewModel.getItemsByCategoryAndValidStatus(selectedCategory).observe(this, Observer { itemList ->
                    groceryRVAdapter.updateList(itemList)
                })
            }
        }

    }// Set up radio group to handle expiration status changes
    private fun setupExpirationStatusRadioGroup() {
        expirationStatusRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val isExpired = when (checkedId) {
                R.id.expirationStatusExpired -> true // If "Expired" radio button is selected
                else -> false // Default to valid items
            }

            applyFilters(isExpired) // Apply filters based on the selected expiration status
        }
    }

    // Method to update the item count in the TextView
    private fun updateItemCount(count: Int) {
        itemCountTextView.text = "Total Items: $count" // Update the TextView with the item count
    }

    // Implementation for onDeleteClick method
    override fun onDeleteClick(currentItem: GroceryItems) {
        groceryViewModel.delete(currentItem)  // Delete the item via the ViewModel
        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()  // Provide feedback to the user
    }

}
