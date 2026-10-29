package com.example.kawansekolah.data.model

enum class NotificationType {
    LOGIN,
    TASK,
    AI,
    AI_RUNNING,
    GENERAL
}

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL,
    val relatedTaskId: Int? = null
) {
    fun getFormattedTimestamp(nowMillis: Long = System.currentTimeMillis()): String {
        val diffMillis = nowMillis - timestampMillis
        if (diffMillis < 0) return "Baru saja"

        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        return when {
            diffSeconds < 60 -> "Baru saja"
            diffMinutes < 60 -> "$diffMinutes mnt lalu"
            diffHours < 24 -> "$diffHours jam lalu"
            else -> "$diffDays hr lalu"
        }
    }

    val timestamp: String
        get() = getFormattedTimestamp()

    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (type == NotificationType.LOGIN) {
            return (nowMillis - timestampMillis) >= 3_600_000L
        }
        return false
    }
}
