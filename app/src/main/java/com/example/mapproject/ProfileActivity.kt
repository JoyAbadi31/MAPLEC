package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mapproject.dialogs.VendorBindDialog
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var standLocationTextView: TextView
    private lateinit var chatAdminTextView: TextView
    private lateinit var bindVendorTextView: TextView

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
    }

    private fun setupClickListeners() {
        chatAdminTextView.setOnClickListener {
            startActivity(Intent(this, ChatAdminActivity::class.java))
        }

        bindVendorTextView.setOnClickListener {
            VendorBindDialog().show(supportFragmentManager, "VendorBindDialog")
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
}
