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
                        // Simpan data pengguna ke Firebase Realtime Database
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        val user = User(name, standLocation, email)

                        FirebaseDatabase.getInstance().getReference("users")
                            .child(userId!!)
                            .setValue(user)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish() // Tutup aktivitas setelah sukses
                                } else {
                                    Toast.makeText(this, "Gagal menyimpan data: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Data class untuk struktur data pengguna
    data class User(val name: String, val standLocation: String, val email: String)
}

