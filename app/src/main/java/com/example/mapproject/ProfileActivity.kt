package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mapproject.dialogs.VendorBindDialog
import com.google.firebase.database.*
import com.example.mapproject.models.User
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var standLocationTextView: TextView
    private lateinit var chatAdminTextView: TextView
    private lateinit var bindVendorTextView: TextView
    private lateinit var logoutTextView: TextView
    private lateinit var conditionalLayout: FrameLayout
    private lateinit var vendorDetailsContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupClickListeners()
        loadUserData()
    }

    private fun initializeViews() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        standLocationTextView = findViewById(R.id.standLocationTextView)
        chatAdminTextView = findViewById(R.id.chatAdminTextView)
        bindVendorTextView = findViewById(R.id.bindVendorTextView)
        logoutTextView = findViewById(R.id.logoutTextView)
        conditionalLayout = findViewById(R.id.conditionalLayout)
        vendorDetailsContainer = findViewById(R.id.vendorDetailsContainer)
    }

    private fun setupClickListeners() {
        chatAdminTextView.setOnClickListener {
            startActivity(Intent(this, ChatAdminActivity::class.java))
            finish()
        }

        bindVendorTextView.setOnClickListener {
            VendorBindDialog().show(supportFragmentManager, "VendorBindDialog")
        }

        logoutTextView.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Welcome::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val userCustomId = intent.getStringExtra("USER_CUSTOM_ID")

        if (userCustomId != null) {
            databaseReference.child(userCustomId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value?.toString() ?: "Unknown"
                        val email = snapshot.child("email").value?.toString() ?: "Unknown"
                        val standLocation = snapshot.child("standLocation").value?.toString() ?: "Unknown"

                        nameTextView.text = "Name: $name"
                        emailTextView.text = "Email: $email"
                        standLocationTextView.text = "Stand Location: $standLocation"
                    } else {
                        Toast.makeText(this@ProfileActivity, "User data not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

//    private fun setupConditionalViews(snapshot: DataSnapshot) {
//        val vendorBoundTo = snapshot.child("vendorBoundTo").value?.toString()
//
//        if (vendorBoundTo.isNullOrEmpty()) {
//            bindVendorTextView.visibility = TextView.VISIBLE
//            conditionalLayout.visibility = FrameLayout.GONE // Hide the vendor profile area
//        } else {
//            bindVendorTextView.visibility = TextView.GONE
//            conditionalLayout.visibility = FrameLayout.VISIBLE // Show the vendor profile and menu
//
//            showVendorProfileLayout()
//            showVendorMenus()
//        }
//    }
//
//    private fun showVendorProfileLayout() {
//        // Inflate the layout for the vendor profile
//        val inflater = LayoutInflater.from(this)
//        val vendorProfileView = inflater.inflate(R.layout.infernal_activity_vendor_profile, null)
//
//        // Add the inflated layout to the container
//        vendorDetailsContainer.removeAllViews() // Remove any previous view (if needed)
//        vendorDetailsContainer.addView(vendorProfileView)
//    }
//
//    private fun showVendorMenus() {
//        // Inflate the layout for the menu items button
//        val inflater = LayoutInflater.from(this)
//        val vendorMenuButton = inflater.inflate(R.layout.infernal_activity_vendor_menu_button, null)
//
//        // Add the menu button to the container
//        vendorDetailsContainer.addView(vendorMenuButton)
//    }
}
