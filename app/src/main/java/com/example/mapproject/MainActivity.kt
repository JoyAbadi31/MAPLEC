package com.example.mapproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapproject.adapters.VendorAdapter
import com.example.mapproject.models.Vendor
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

        navProfile.setOnClickListener {
            // Check authentication only when accessing profile
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                // User is logged in, navigate to Profile
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("userName", currentUser.displayName)
                    putExtra("userEmail", currentUser.email)
                    putExtra("userUid", currentUser.uid)
                }
                startActivity(intent)
            } else {
                // User is not logged in, redirect to Login
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}