package com.jeff.geminidemo.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jeff.geminidemo.databinding.ItemBotMessageBinding
import com.jeff.geminidemo.databinding.ItemUserMessageBinding
import com.jeff.geminidemo.dataclass.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val binding = ItemUserMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            UserViewHolder(binding)
        } else {
            val binding = ItemBotMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            BotViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.binding.textViewUserMessage.text = message.message
        } else if (holder is BotViewHolder) {
            holder.binding.textViewBotMessage.text = message.message
            holder.binding.copyIcon.setOnClickListener {
                copyToClipboard(holder.binding.root.context, message.message)
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Bot Response", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    class UserViewHolder(val binding: ItemUserMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    class BotViewHolder(val binding: ItemBotMessageBinding) :
        RecyclerView.ViewHolder(binding.root)
}