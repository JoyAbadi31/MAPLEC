package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val logTag = "LoginActivity"
    private val database = FirebaseDatabase.getInstance()
    private var userCustomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        val currentUser = auth.currentUser
        val loginButton = findViewById<Button>(R.id.loginButton)

        if (currentUser != null) {
            // If the user is already logged in, disable the login button
            loginButton.isEnabled = false
            Toast.makeText(this, "Already logged in as ${currentUser.email}", Toast.LENGTH_SHORT).show()
        }

        setupViews()
    }

    private fun setupViews() {
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerNow = findViewById<TextView>(R.id.registerNow)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        loginButton.setOnClickListener {
            handleLogin(
                emailField.text.toString().trim(),
                passwordField.text.toString().trim(),
                loginButton
            )
        }

        registerNow.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        backButton.setOnClickListener { finish() }
    }

    private fun handleLogin(email: String, password: String, loginButton: Button) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled = false
        loginButton.setBackgroundColor(resources.getColor(com.google.android.material.R.color.material_on_primary_disabled)) // Disable button visual
        Log.d(logTag, "Starting login process...")

        lifecycleScope.launch {
            try {
                // Sign in with Firebase Authentication
                Log.d(logTag, "Attempting Firebase Authentication")
                auth.signInWithEmailAndPassword(email, password).await()

                // Query the database in a background thread
                Log.d(logTag, "Querying database for user data")
                val snapshots = withContext(Dispatchers.IO) {
                    database.reference.child("users").orderByChild("email").equalTo(email).get().await()
                }

                // Iterate through the snapshots to find the matching user
                Log.d(logTag, "Processing database results...")
                for (snapshot in snapshots.children) {
                    val userEmail = snapshot.child("email").value as? String
                    if (userEmail == email) {
                        userCustomId = snapshot.key
                        Log.d(logTag, "User found, starting Welcome Activity")
                        val intent = Intent(this@LoginActivity, Welcome::class.java)
                        intent.putExtra("USER_CUSTOM_ID", userCustomId)

                        // Set flags to clear the back stack and prevent going back
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        // Only start the new activity if the LoginActivity is still valid
                        if (!isFinishing && !isDestroyed) {
                            startActivity(intent)
                            finish()
                        }

                        return@launch  // Ensure we exit the coroutine after starting Welcome Activity
                    }
                }

                // If no user found
                Toast.makeText(
                    this@LoginActivity,
                    "Data pengguna tidak ditemukan!",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e(logTag, "Error: ${e.message}", e)
                Toast.makeText(this@LoginActivity, e.message ?: "Login gagal.", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                // Only update the UI if the LoginActivity is still valid
                if (!isFinishing && !isDestroyed) {
                    loginButton.isEnabled = true
                    loginButton.setBackgroundColor(resources.getColor(R.color.primary_color)) // Restore button color
                }
            }
        }
    }

}
