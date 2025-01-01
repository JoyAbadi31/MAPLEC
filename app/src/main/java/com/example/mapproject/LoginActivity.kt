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
    private val TAG = "LoginActivity"
    private val database = FirebaseDatabase.getInstance()
    private var userCustomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
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

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                auth.signInWithEmailAndPassword(email, password).await()

                val snapshots = withContext(Dispatchers.IO) {
                    database.reference.child("users").get().await()
                }

                snapshots.children.forEach { snapshot ->
                    val userEmail = snapshot.child("email").value as? String
                    if (userEmail == email) {
                        userCustomId = snapshot.key
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_CUSTOM_ID", userCustomId)
                        startActivity(intent)
                        finish()
                        return@forEach
                    }
                }

                Toast.makeText(
                    this@LoginActivity,
                    "Data pengguna tidak ditemukan!",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(this@LoginActivity, e.message ?: "Login gagal.", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                loginButton.isEnabled = true
            }
        }
    }
}