package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapproject.adapters.VendorAdapter
import com.example.mapproject.models.Vendor
import com.example.mapproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerViewTitle: TextView
    private lateinit var navHome: AppCompatImageButton
    private lateinit var navProfile: AppCompatImageButton
    private lateinit var recyclerView: RecyclerView

    private val randomMessages = listOf(
        "Cek yuk! ada makanan apa di UMN!",
        "Mau makan apa hari ini?",
        "Ngapain jauh-jauh? Makan di UMN aja!",
        "Temukan makanan favoritmu di UMN!",
        "Sudah lapar? Lihat pilihan makanan di UMN!"
    )

    private val handler = Handler(Looper.getMainLooper())
    private val database = FirebaseDatabase.getInstance()

    private val vendorClickCallback: (Vendor) -> Unit = { vendor ->
        val intent = Intent(this, TenantProfile::class.java)
        intent.putExtra("vendor", vendor)
        Log.d("MainActivity", "Passing vendor: $vendor")
        startActivity(intent)
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupNavigation()
        startMessageUpdate()
        fetchVendorsWithCoroutines()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.mainPageVendorRecyclerView)
        recyclerViewTitle = findViewById(R.id.recyclerViewTitle)
        navHome = findViewById(R.id.nav_home)
        navProfile = findViewById(R.id.nav_profile)
//        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = VendorAdapter(emptyList(), vendorClickCallback)
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            // Simply refresh the data
            fetchVendorsWithCoroutines()
        }

        updateProfileButtonState()

        navProfile.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                lifecycleScope.launch {
                    try {
                        // Step 1: Fetch the custom user ID from userIdMapping
                        val customUserId = withContext(Dispatchers.IO) {
                            database.reference.child("userIdMapping").child(currentUser.uid).get().await().value.toString()
                        }

                        // Step 2: Use customUserId to fetch user data from the 'users' node
                        val userSnapshot = withContext(Dispatchers.IO) {
                            database.reference.child("users").child(customUserId).get().await()
                        }

                        if (userSnapshot.exists()) {
                            val userData = userSnapshot.getValue(User::class.java)
                            Log.d("MainActivity", "User data: $userData")

                            val intent = Intent(this@MainActivity, ProfileActivity::class.java).apply {
                                putExtra("USER_CUSTOM_ID", userData?.userId ?: "")
                                putExtra("name", userData?.name ?: "Unknown")
                                putExtra("email", userData?.email ?: "Unknown")
                                putExtra("standLocation", userData?.standLocation ?: "Unknown")
                                putExtra("vendorBoundTo", userData?.vendorBoundTo ?: "Unknown")
                            }
                            startActivity(intent)
                        } else {
                            Log.e("MainActivity", "No data found for custom userId: $customUserId")
                            Toast.makeText(this@MainActivity, "No user data found", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to fetch user data: ${e.message}")
                        Toast.makeText(this@MainActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }


    }

    private fun fetchVendorsWithCoroutines() {

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val vendorList = withContext(Dispatchers.IO) {
                    val snapshot = database.reference.child("vendors").get().await()
                    snapshot.children.mapNotNull { it.getValue(Vendor::class.java) }
                }

                recyclerView.adapter = VendorAdapter(vendorList, vendorClickCallback)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching vendors: ${e.message}")
                // Show error message to user if needed
            } finally {
                Log.d("Waited long enough huh?", "Finally!")
            }
        }
    }

    private fun startMessageUpdate() {
        recyclerViewTitle.text = getRandomMessage()
        handler.postDelayed(object : Runnable {
            override fun run() {
                recyclerViewTitle.text = getRandomMessage()
                handler.postDelayed(this, 3000)
            }
        }, 3000)
    }

    private fun getRandomMessage(): String {
        var randomMessage: String
        do {
            randomMessage = randomMessages[Random.nextInt(randomMessages.size)]
        } while (randomMessage == recyclerViewTitle.text.toString())
        return randomMessage
    }

    private fun updateProfileButtonState() {
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        navProfile.isEnabled = true // Keep enabled to allow login redirect
        navProfile.setColorFilter(
            if (isLoggedIn) getColor(android.R.color.white)
            else getColor(android.R.color.darker_gray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}