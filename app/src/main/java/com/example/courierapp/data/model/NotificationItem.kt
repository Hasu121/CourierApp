package com.example.courierapp.data.model

data class NotificationItem(
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val relatedBookingId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)