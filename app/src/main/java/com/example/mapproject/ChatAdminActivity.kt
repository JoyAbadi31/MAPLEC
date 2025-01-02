package com.example.mapproject

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapproject.adapters.ChatAdapter
import com.example.mapproject.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChatAdminActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val channelsRef = database.reference.child("channels")
    private val channelCounterRef = database.reference.child("channelCounter")
    private val userIdMappingRef = database.reference.child("userIdMapping")

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private var currentChannelId: String = ""

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_admin)

        setupUI()

        lifecycleScope.launch {
            currentChannelId = getOrCreateChannelId()
            fetchMessages()
        }
    }

    private fun setupUI() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(::formatDatetimeForDisplay) // Pass the formatter function to the adapter
        chatRecyclerView.adapter = chatAdapter

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMessages() {
        channelsRef.child(currentChannelId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull { it.getValue<Message>() }
                    chatAdapter.submitList(messages)
                    chatRecyclerView.scrollToPosition(messages.size - 1) // Auto-scroll to the latest message
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatAdminActivity", "Error fetching messages: ${error.message}")
                }
            })
    }

    private fun sendMessage(content: String) {
        val message = Message(
            content = content,
            datetime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
            sender = currentUser?.uid ?: "Unknown",
            receiver = "Admin"
        )

        lifecycleScope.launch {
            try {
                val newMessageRef = channelsRef.child(currentChannelId).child("messages").push()
                newMessageRef.setValue(message).await()
            } catch (e: Exception) {
                Log.e("ChatAdminActivity", "Error sending message: ${e.message}")
                Toast.makeText(this@ChatAdminActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getOrCreateChannelId(): String {
        val userId = currentUser?.uid ?: return "CH000" // Default to CH000 if user ID is null
        return try {
            // Step 1: Get the user-friendly name from the userIdMapping (using the helper function)
            val senderName = getUserFriendlyNameFromMapping(userId)

            // Step 2: Fetch all channels and filter them to find one with both the current user and "Admin"
            val allChannelsSnapshot = channelsRef.get().await() // Fetch all channels from Firebase

            var existingChannelId: String? = null
            var defaultChannelExists = false
            allChannelsSnapshot.children.forEach { channelSnapshot ->
                val users = channelSnapshot.child("users").children.map { it.value.toString() }
                if (users.contains(userId) && users.contains("Admin")) {
                    existingChannelId = channelSnapshot.key // Set the existing channel ID
                    return@forEach // Break out of the loop if found
                }
                if (channelSnapshot.key == "CH000") {
                    defaultChannelExists = true // Mark if the default channel exists
                }
            }

            // Step 3: If an existing channel is found, return its ID
            if (existingChannelId != null) {
                return existingChannelId!! // Explicitly cast to String
            }

            // Step 4: If no existing channel is found, create a new one
            val channelCounterRef = database.reference.child("channelCounter")
            val counterSnapshot = channelCounterRef.get().await() // Get the current counter value from Firebase
            var currentCounter = counterSnapshot.getValue<Int>() ?: 0

            // If the default channel (CH000) exists, subtract 1 from the counter
            if (defaultChannelExists) {
                currentCounter -= 1
            }

            val newChannelId = "CH${String.format("%03d", currentCounter + 1)}" // Format the new channel ID

            // Atomically update the counter in Firebase
            channelCounterRef.setValue(currentCounter + 1).await()

            // Step 5: Create a new channel node with the current user and admin
            val newUserChannelRef = channelsRef.child(newChannelId)
            newUserChannelRef.child("users").setValue(listOf(userId, "Admin")).await()

            // Return the new channel ID
            newChannelId
        } catch (e: Exception) {
            Log.e("ChatAdminActivity", "Error generating or fetching channel ID: ${e.message}")
            return "CH000" // Return a default channel ID in case of error
        }
    }

    private fun formatDatetimeForDisplay(datetime: String): String {
        return try {
            // Parse the original ISO format datetime string
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta") // Input datetime is in UTC+7
            val date = inputFormat.parse(datetime)

            // Format to Indonesian time (Jakarta, WIB) with 24-hour format
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta") // Set to Jakarta timezone (UTC+7)
            outputFormat.format(date ?: datetime) // Handle null date gracefully
        } catch (e: Exception) {
            Log.e("ChatAdminActivity", "Error formatting datetime: ${e.message}")
            datetime // Return the original datetime string if parsing fails
        }
    }


    private suspend fun getUserFriendlyNameFromMapping(userId: String): String {
        return try {
            // Step 1: Fetch the custom user ID from userIdMapping
            val customUserId = withContext(Dispatchers.IO) {
                database.reference.child("userIdMapping").child(userId).get().await().value.toString()
            }

            // Step 2: Use customUserId to fetch user data from the 'users' node
            val userSnapshot = withContext(Dispatchers.IO) {
                database.reference.child("users").child(customUserId).get().await()
            }

            // Assuming that the 'users' node has a field 'username' or some other user-friendly name
            val name = userSnapshot.child("name").getValue<String>() ?: "Unknown User"
            name
        } catch (e: Exception) {
            Log.e("ChatAdminActivity", "Error fetching user-friendly name: ${e.message}")
            "Unknown User" // Default in case of any error
        }
    }

}
