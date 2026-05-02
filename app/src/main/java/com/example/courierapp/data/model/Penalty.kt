package com.example.courierapp.data.model

data class Penalty(
    val penaltyId: String = "",
    val userId: String = "",
    val role: String = "",
    val bookingId: String = "",
    val amount: Double = 0.0,
    val reason: String = "",
    val createdAt: Long = 0L
)