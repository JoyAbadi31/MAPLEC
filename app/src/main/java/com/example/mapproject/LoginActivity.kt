package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Get references to UI elements
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerNow = findViewById<TextView>(R.id.registerNow)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Authenticate user asynchronously
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Authentication successful, get the current user
                        val userId = auth.currentUser?.uid
                        val userRef = FirebaseDatabase.getInstance().getReference("users")

                        // Querying user by email with better async handling
                        userRef.orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        // Find the matching user and proceed
                                        val userSnapshot = snapshot.children.first()
                                        val userIdFromDb = userSnapshot.key  // This is your custom UID

                                        // Proceed to the main activity
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish() // Close LoginActivity
                                    } else {
                                        // Show error if user data is not found
                                        Toast.makeText(this@LoginActivity, "Data pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@LoginActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    } else {
                        // If authentication fails
                        val errorMessage = task.exception?.localizedMessage ?: "Login gagal."
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
        }


        // Handle register now text click
        registerNow.setOnClickListener {
            // Navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle back button click
        backButton.setOnClickListener {
            // Finish the activity or navigate to a previous screen
            finish()
        }
    }
}
