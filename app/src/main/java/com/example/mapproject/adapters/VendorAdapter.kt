package com.example.mapproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapproject.R
import com.example.mapproject.models.Vendor

class VendorAdapter(private val vendors: List<Vendor>, private val onVendorClick: (Vendor) -> Unit) : RecyclerView.Adapter<VendorAdapter.VendorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_vendor, parent, false)
        return VendorViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        val currentVendor = vendors[position]
        holder.bind(currentVendor, onVendorClick)  // Pass the callback to bind()
    }

    override fun getItemCount() = vendors.size

    class VendorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewVendorName: TextView = itemView.findViewById(R.id.vendorName)
        val textViewVendorDescription: TextView = itemView.findViewById(R.id.vendorDescription)
        val buttonVendorStatus: Button = itemView.findViewById(R.id.buttonVendorStatus)
        val buttonView: Button = itemView.findViewById(R.id.buttonView)

        fun bind(vendor: Vendor, onVendorClick: (Vendor) -> Unit) {
            // Bind data to UI components
            textViewVendorName.text = vendor.name
            textViewVendorDescription.text = vendor.description

            // Load the profile picture using Glide
            Glide.with(itemView.context)
                .load(vendor.profilePictureURL)
                .into(imageView)

            // Set the status ("BUKA" or "TUTUP") based on isSelling boolean
            if (vendor.isSelling == true) {
                buttonVendorStatus.text = "BUKA"
                buttonVendorStatus.setBackgroundColor(itemView.context.getColor(R.color.hijau_khas)) // Set background to green
            } else {
                buttonVendorStatus.text = "TUTUP"
                buttonVendorStatus.setBackgroundColor(itemView.context.getColor(R.color.merah_khas)) // Set background to red
            }

            // Make the button unclickable
            buttonVendorStatus.isClickable = false

            // Set the "View" button click listener
            buttonView.setOnClickListener {
                onVendorClick(vendor) // Trigger the callback with the vendor data
            }
        }
    }
}

