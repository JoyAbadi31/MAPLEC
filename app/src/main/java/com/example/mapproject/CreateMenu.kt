package com.example.mapproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class CreateMenu : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_menu)

        database = Firebase.database.reference.child("menuItems")
        storage = Firebase.storage

        imageView = findViewById(R.id.imageView)
        val selectImageButton = findViewById<Button>(R.id.selectImage)
        val saveButton = findViewById<Button>(R.id.buttonSave)
        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val descriptionEditText = findViewById<EditText>(R.id.editTextDescription)
        val profileButton = findViewById<Button>(R.id.profileButton)

        selectImageButton.setOnClickListener {
            showImageSourceOptions()
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (::imageUri.isInitialized && name.isNotEmpty() && description.isNotEmpty()) {
                uploadImageToFirebaseStorage { imageUrl ->
                    val itemId = database.push().key!!
                    val menuItem = MenuItem(itemId, name, description, imageUrl)
                    database.child(itemId).setValue(menuItem).addOnSuccessListener {
                        Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show()
                        nameEditText.text.clear()
                        descriptionEditText.text.clear()
                        imageView.setImageResource(R.drawable.icon_paket)
                        imageUri = Uri.EMPTY
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        profileButton.setOnClickListener {
            // Navigasi ke ProfileActivity
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun showImageSourceOptions() {
        val options = arrayOf("Camera", "Gallery")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
            val uri = getImageUriFromBitmap(bitmap)
            uri?.let {
                imageUri = it
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "CapturedImage", null)
        return Uri.parse(path)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data!!
            imageView.setImageURI(imageUri)
        }
    }

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
        const val CAMERA_PERMISSION_CODE = 2000
    }


}
