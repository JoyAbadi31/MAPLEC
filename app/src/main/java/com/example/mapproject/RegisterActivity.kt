package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Mendapatkan referensi komponen di layout
        val nameField = findViewById<EditText>(R.id.nameField)
        val standLocationField = findViewById<EditText>(R.id.standLocationField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Tombol Kembali
        backButton.setOnClickListener {
            finish() // Menutup aktivitas saat tombol kembali ditekan
        }

        // Tombol Register
        registerButton.setOnClickListener {
            val name = nameField.text.toString()
            val standLocation = standLocationField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            // Validasi input
            if (name.isEmpty() || standLocation.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proses registrasi dengan Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Retrieve the current user count from the Firebase database
                        val usersRef = FirebaseDatabase.getInstance().getReference("users")
                        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Get the current number of users in the database
                                val userCount = snapshot.childrenCount
                                // Generate custom UID based on the count (e.g., U00000000000001)
                                val customUID =
                                    generateCustomUID(userCount.toInt() + 1) // Increment count for new user

                                // Create a new user object
                                val user = User(customUID, name, standLocation, email, "")

                                // Save the user under the custom UID
                                usersRef.child(customUID).setValue(user)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "Registrasi berhasil",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            startActivity(
                                                Intent(
                                                    this@RegisterActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finish() // Tutup aktivitas setelah sukses
                                        } else {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "Gagal menyimpan data: ${it.exception?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Gagal mendapatkan data pengguna: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    } else {
                        Toast.makeText(
                            this,
                            "Registrasi gagal: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // Function to generate a custom UID, using the user count
    private fun generateCustomUID(count: Int): String {
        // Format count into a 14-digit number and prepend 'U'
        val formattedCount = String.format("%014d", count)
        return "U$formattedCount"
    }

    // Data class for user structure
    data class User(
        val userId: String, // Custom generated UID
        val name: String,
        val standLocation: String,
        val email: String,
        var vendorBoundTo: String // Vendor the user is bound to
    )
}
