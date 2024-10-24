package com.example.mapproject


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class CreateMenu : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_menu)

        // Inisialisasi Firebase Database dan Storage
        database = Firebase.database.reference.child("menuItems")
        storage = Firebase.storage

        // Inisialisasi View
        imageView = findViewById(R.id.imageView)
        val selectImageButton = findViewById<Button>(R.id.selectImage)
        val saveButton = findViewById<Button>(R.id.buttonSave)
        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val descriptionEditText = findViewById<EditText>(R.id.editTextDescription)

        // Aksi memilih gambar dari galeri
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Aksi menyimpan data ke Firebase
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (::imageUri.isInitialized && name.isNotEmpty() && description.isNotEmpty()) {
                uploadImageToFirebaseStorage { imageUrl ->
                    val itemId = database.push().key!!
                    val menuItem = MenuItem(itemId, name, description, imageUrl)
                    database.child(itemId).setValue(menuItem).addOnSuccessListener {
                        // Beri tahu pengguna bahwa item berhasil disimpan
                        Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show()

                        // Kosongkan form
                        nameEditText.text.clear()
                        descriptionEditText.text.clear()
                        imageView.setImageResource(R.drawable.icon_paket) // Reset image ke placeholder
                        imageUri = Uri.EMPTY // Reset imageUri ke null
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Fungsi untuk menangani hasil pemilihan gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data!!
            imageView.setImageURI(imageUri)
        }
    }

    // Fungsi untuk mengupload gambar ke Firebase Storage
    private fun uploadImageToFirebaseStorage(onComplete: (String) -> Unit) {
        val storageRef = storage.reference.child("images/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                onComplete(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val IMAGE_PICK_CODE = 1000
    }
}