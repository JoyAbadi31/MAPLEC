package com.example.mapproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MenuAdapter(private val menuList: List<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuList[position]
        holder.nameTextView.text = menuItem.name
        holder.descriptionTextView.text = menuItem.description

        // Load gambar dari URL menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(menuItem.imageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.itemDescription)
    }
}