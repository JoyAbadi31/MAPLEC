package com.example.mapproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapproject.R
import com.example.mapproject.models.Message

class ChatAdapter(private val formatDatetime: (String) -> String) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private val messages = mutableListOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message, formatDatetime)
    }

    override fun getItemCount(): Int = messages.size

    fun submitList(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val datetimeTextView: TextView = itemView.findViewById(R.id.messageSentDatetimeTextView)

        fun bind(message: Message, formatDatetime: (String) -> String) {
            messageTextView.text = message.content
            datetimeTextView.text = formatDatetime(message.datetime)
        }
    }
}

