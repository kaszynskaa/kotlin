package com.s21845.digitaldiary.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.s21845.digitaldiary.R
import com.s21845.digitaldiary.models.HappyPlaceModel

class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPlaceImage: ImageView = view.findViewById(R.id.iv_place_image)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_happy_place,
            parent,
            false
        )
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        holder.tvTitle.text = model.title

        if (!model.image.isNullOrEmpty()) {
            Glide.with(context)
                .load(model.image)
                .into(holder.ivPlaceImage)
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    fun setOnEditListener(onEdit: (position: Int) -> Unit) {
        this.onEditListener = onEdit
    }

    private var onEditListener: ((Int) -> Unit)? = null

    inner class SwipeToEditCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            onEditListener?.invoke(position)
        }
    }

    fun attachSwipeHandler(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(SwipeToEditCallback())
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
