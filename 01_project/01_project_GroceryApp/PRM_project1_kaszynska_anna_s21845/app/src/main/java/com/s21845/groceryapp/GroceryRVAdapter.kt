package com.s21845.groceryapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class GroceryRVAdapter(
    private var list: List<GroceryItems>,
    private val groceryItemClickInterface: GroceryItemClickInterface
) : RecyclerView.Adapter<GroceryRVAdapter.GroceryViewHolder>() {

    inner class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView = itemView.findViewById(R.id.idtvitemname)
        val quantityTV: TextView = itemView.findViewById(R.id.idtvquantity)
        val rateTV: TextView = itemView.findViewById(R.id.idtvrate)
        val totalTV: TextView = itemView.findViewById(R.id.idtvtotalamount)
        val expirationStatusTV: TextView = itemView.findViewById(R.id.idtvexpirationstatus)
        val deleteIV: ImageView = itemView.findViewById(R.id.idivdelete)
        val categoryTV: TextView = itemView.findViewById(R.id.idtvCategory)
        val expirationDateTV: TextView = itemView.findViewById(R.id.idtvExpirationDate)
        val itemPhotoImageView: ImageView = itemView.findViewById(R.id.itemPhotoImageView) // New ImageView for item photo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grocery_rv_item, parent, false)
        return GroceryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val currentItem = list[position]

        // Set text fields
        holder.nameTV.text = currentItem.itemName
        holder.quantityTV.text = currentItem.itemQuantity.toString()
        holder.rateTV.text = "₹: ${currentItem.itemPrice}"
        val itemTotal = currentItem.itemQuantity * currentItem.itemPrice
        holder.totalTV.text = "₹: $itemTotal"

        // Set expiration status and date
        val expirationStatus = if (currentItem.isExpired()) "Expired" else "Valid"
        holder.expirationStatusTV.text = expirationStatus
        holder.expirationDateTV.text = (currentItem.expirationDate ?: "No Expiration Date").toString()

        // Set category text
        holder.categoryTV.text = currentItem.selectedCategory ?: "Category Unknown"

        // Set item photo from `imageUri`
        if (currentItem.selectedImageUri != null) {
            val imageUri = Uri.parse(currentItem.selectedImageUri)
            holder.itemPhotoImageView.setImageURI(imageUri) // Set the product photo
        } else {
            holder.itemPhotoImageView.setImageResource(R.drawable.product) // Default image if no `imageUri`
        }

        // Set click listener for deletion
        holder.deleteIV.setOnClickListener {
            groceryItemClickInterface.onDeleteClick(currentItem) // Handle deletion
        }

        // Set item click listener
        holder.itemView.setOnClickListener {
            groceryItemClickInterface.onItemClick(currentItem)
        }

        // Long-click listener for extra actions
        holder.itemView.setOnLongClickListener {
            if (currentItem.isExpired()) {
                groceryItemClickInterface.markAsThrownAway(currentItem)
                Toast.makeText(holder.itemView.context, "Item is expired and has been thrown away.", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Make Item Changes")
                    .setMessage("Are you sure you want to make changes to this item?")
                    .setPositiveButton("Yes") { _, _ ->
                        groceryItemClickInterface.onItemClick(currentItem)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
            true
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<GroceryItems>) {
        list = newList
        notifyDataSetChanged() // Notify that the list has changed
    }
}
