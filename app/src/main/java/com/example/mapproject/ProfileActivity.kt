package com.example.mapproject

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var standLocationTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users") // Sesuai dengan struktur database Anda

        // Inisialisasi Views
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        standLocationTextView = findViewById(R.id.standLocationTextView)

        // Ambil UID pengguna
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            Log.d("ProfileActivity", "userId: $uid") // Debug UID

            // Ambil data pengguna dari Firebase
            databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ProfileActivity", "Snapshot: $snapshot") // Debug data snapshot

                    if (snapshot.exists()) {
                        // Ambil data dari snapshot
                        val name = snapshot.child("name").value?.toString() ?: "Unknown"
                        val email = snapshot.child("email").value?.toString() ?: "Unknown"
                        val standLocation = snapshot.child("standLocation").value?.toString() ?: "Unknown"

                        // Debug nilai yang didapat
                        Log.d("ProfileActivity", "Name: $name, Email: $email, Stand Location: $standLocation")

                        // Tampilkan data di TextView
                        nameTextView.text = "Name: $name"
                        emailTextView.text = "Email: $email"
                        standLocationTextView.text = "Stand Location: $standLocation"
                    } else {
                        Log.e("ProfileActivity", "Users data not found!")
                        Toast.makeText(this@ProfileActivity, "User data not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileActivity", "Database error: ${error.message}")
                    Toast.makeText(this@ProfileActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.e("ProfileActivity", "No logged-in user!")
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_SHORT).show()
        }
    }
}
