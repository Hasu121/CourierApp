package com.example.courierapp.data.model

data class BookingStatusLog(
    val bookingId: String = "",
    val status: String = "",
    val changedBy: String = "",
    val note: String = "",
    val timestamp: Long = 0L
)