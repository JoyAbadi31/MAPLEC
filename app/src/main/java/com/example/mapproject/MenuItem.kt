package com.example.mapproject

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val description: String = "",
    val imageUrl: String = "",
    val belongingTo: Int // Indicate which vendor this item belongs to.
)