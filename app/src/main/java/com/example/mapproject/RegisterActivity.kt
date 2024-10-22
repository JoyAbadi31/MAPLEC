package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var formContainer: FrameLayout
    private lateinit var registerButton: Button
    private lateinit var vendorCheckbox: CheckBox
    private lateinit var backButton: Button // Add back button variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI components
        formContainer = findViewById(R.id.form_container)
        registerButton = findViewById(R.id.registerButton)
        vendorCheckbox = findViewById(R.id.vendorCheckbox)
        backButton = findViewById(R.id.backButton) // Initialize back button

        // Inflate user registration form by default
        inflateForm(R.layout.register_user)

        // Toggle between user and vendor registration forms
        vendorCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inflateForm(R.layout.register_vendor)
            } else {
                inflateForm(R.layout.register_user)
            }
        }

        // Handle registration button click
        registerButton.setOnClickListener {
            if (vendorCheckbox.isChecked) {
                handleVendorRegistration()
            } else {
                handleUserRegistration()
            }
        }

        // Handle back button click
        backButton.setOnClickListener {
            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close RegisterActivity
        }
    }

    // Function to inflate the selected registration form
    private fun inflateForm(layoutId: Int) {
        val inflater = LayoutInflater.from(this)
        formContainer.removeAllViews()
        inflater.inflate(layoutId, formContainer, true)
    }

    // Function to handle regular user registration
    private fun handleUserRegistration() {
        val nameField = findViewById<EditText>(R.id.nameField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val confirmPasswordField = findViewById<EditText>(R.id.confirmPasswordField)

        val name = nameField.text.toString()
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        val confirmPassword = confirmPasswordField.text.toString()

        if (validateInputs(name, email, password, confirmPassword)) {
            Toast.makeText(this, "User Registered Successfully!", Toast.LENGTH_SHORT).show()
            finish() // Close the RegisterActivity and return to MainActivity
        }
    }

    // Function to handle vendor registration
    private fun handleVendorRegistration() {
        val nameField = findViewById<EditText>(R.id.nameField)
        val standLocationField = findViewById<EditText>(R.id.standLocationField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val confirmPasswordField = findViewById<EditText>(R.id.confirmPasswordField)

        val name = nameField.text.toString()
        val standLocation = standLocationField.text.toString()
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        val confirmPassword = confirmPasswordField.text.toString()

        if (validateInputs(name, email, password, confirmPassword)) {
            Toast.makeText(this, "Vendor Registered Successfully!", Toast.LENGTH_SHORT).show()
            finish() // Close the RegisterActivity and return to MainActivity
        }
    }

    // Function to validate inputs for both user and vendor
    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
