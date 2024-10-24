package com.example.mapproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ValueEventListener

class TenantProfile : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var menuList: MutableList<MenuItem>
    private lateinit var menuAdapter: MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_profile)

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        menuList = mutableListOf()
        menuAdapter = MenuAdapter(menuList)
        recyclerView.adapter = menuAdapter

        // Inisialisasi Database Firebase
        database = Firebase.database.reference.child("menuItems")

        // Ambil data dari Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuList.clear()
                for (dataSnapshot in snapshot.children) {
                    val menuItem = dataSnapshot.getValue(MenuItem::class.java)
                    if (menuItem != null) {
                        menuList.add(menuItem)
                    }
                }
                // Notifikasi adapter untuk memperbarui tampilan
                menuAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error jika terjadi
            }
        })
    }
}