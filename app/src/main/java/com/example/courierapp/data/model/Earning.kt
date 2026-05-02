package com.example.courierapp.data.model

data class Earning(
    val earningId: String = "",
    val driverId: String = "",
    val bookingId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val dropAddress: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "cash",
    val paymentStatus: String = "paid",
    val createdAt: Long = 0L
)