package com.example.kawansekolah.data.model

data class ChatMessage(
    val id: Int,
    val isUser: Boolean,
    val text: String,
    val timestamp: String
)
