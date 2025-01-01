package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapproject.adapters.VendorAdapter
import com.example.mapproject.models.Vendor
import com.google.firebase.database.FirebaseDatabase
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()

    // Define the callback function here so it can be used across the entire class
    private val vendorClickCallback: (Vendor) -> Unit = { vendor ->
        // Handle the "View" button click: navigate to a new activity
        val intent = Intent(this, TenantProfile::class.java)
        intent.putExtra("vendor", vendor) // Pass the vendor to the next activity
        Log.d("MainActivity", "Passing vendor: $vendor")
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize the RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.mainPageVendorRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch the vendors from Firebase
        fetchVendors()

        // Initialize the adapter with an empty list initially
        recyclerView.adapter = VendorAdapter(emptyList(), vendorClickCallback)
    }

    private fun fetchVendors() {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val vendorList = mutableListOf<Vendor>()
                task.result?.children?.forEach { snapshot ->
                    val vendor = snapshot.getValue(Vendor::class.java)
                    vendor?.let { vendorList.add(it) }
                }

                // After fetching the vendors, update the RecyclerView adapter with the new list
                val recyclerView: RecyclerView = findViewById(R.id.mainPageVendorRecyclerView)
                recyclerView.adapter = VendorAdapter(vendorList, vendorClickCallback) // Correctly pass the callback here
            } else {
                Log.e("MainActivity", "Error fetching vendors: ${task.exception?.message}")
            }
        }
    }
}


