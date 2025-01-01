package com.example.mapproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale
import com.example.mapproject.models.MenuItem  // Import the correct MenuItem class

val localeID = Locale("in", "ID")

class MenuAdapter(private val menuList: MutableList<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuList[position]

        // Bind the data to the views
        holder.nameTextView.text = menuItem.itemName
        holder.descriptionTextView.text = menuItem.itemDescription

        // Use %d to format integers (itemPrice should be an integer)
        holder.priceTextView.text = String.format(localeID, "Rp. %,d", menuItem.itemPrice)

        // Use %d to format integers (itemStock should be an integer)
        holder.stockTextView.text = String.format(localeID, "Stock: %d", menuItem.itemStock)

        // Load image from URL using Glide
        Glide.with(holder.itemView.context).load(menuItem.itemPictureURL).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.itemDescription)
        val priceTextView: TextView = itemView.findViewById(R.id.itemPrice)
        val stockTextView: TextView = itemView.findViewById(R.id.itemStock)
    }
}

