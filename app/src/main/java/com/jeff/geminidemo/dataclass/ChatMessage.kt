package com.jeff.geminidemo.dataclass

data class ChatMessage(
    val message: String,
    val isUser: Boolean // true if the message is from the user, false if from the bot
)
