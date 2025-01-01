package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mapproject.models.Vendor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ValueEventListener

class TenantProfile : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var menuList: MutableList<com.example.mapproject.models.MenuItem>
    private lateinit var menuAdapter: MenuAdapter

    private lateinit var nodeKey: String // The node key for the vendor whose menu items we are fetching

    private lateinit var vendorTextView: TextView
    private lateinit var vendorDescriptionTextView: TextView
    private lateinit var vendorBannerView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_profile)

        vendorTextView = findViewById(R.id.vendorName)
        vendorDescriptionTextView = findViewById(R.id.vendorDescription)
        vendorBannerView = findViewById(R.id.vendorBannerView)

        val vendor = intent.getParcelableExtra<Vendor>("vendor")
        Log.d("TenantProfile", "Received vendor: $vendor")
        vendor?.let {
            vendorTextView.text = it.name
            vendorDescriptionTextView.text = it.description
            Glide.with(this).load(it.bannerPictureURL).into(vendorBannerView)
        }

        // Get the node key for the vendor
        val nodeKey = vendor?.nodeKey

        // Back button setup
        val backbtn: ImageButton = findViewById(R.id.back_button)
        backbtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize menuList as an empty list at the beginning
        menuList = mutableListOf()

        // Initialize the menuAdapter with an empty list initially
        menuAdapter = MenuAdapter(menuList)

        // Set the adapter to the RecyclerView
        recyclerView.adapter = menuAdapter

        // Initialize Firebase Database reference for this vendor's menu items
        database = Firebase.database.reference.child("vendors").child(nodeKey.toString()).child("menuItems")

        // Fetch menu items for this vendor from Firebase
        fetchMenuItemsFromDatabase()
    }

    private fun fetchMenuItemsFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the menu list before adding new items
                menuList.clear()

                // Check if the snapshot contains data
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val menuItem = dataSnapshot.getValue(com.example.mapproject.models.MenuItem::class.java)

                        // Ensure the menu item is not null and add it to the list
                        if (menuItem != null) {
                            menuList.add(menuItem)
                        } else {
                            // Log or display a message for empty menu items
                            Toast.makeText(this@TenantProfile, "Empty menu item detected", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Notify the adapter to refresh the UI with the data
                    menuAdapter.notifyDataSetChanged()

                    // If no valid menu items are found, display a message to the user
                    if (menuList.isEmpty()) {
                        Toast.makeText(this@TenantProfile, "No menu items available for this vendor", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Handle case where data does not exist in the database
                    Toast.makeText(this@TenantProfile, "No menu items available for this vendor", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if the data fetching is cancelled
                Toast.makeText(this@TenantProfile, "Error fetching data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
