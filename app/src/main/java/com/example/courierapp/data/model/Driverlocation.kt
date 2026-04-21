package com.example.courierapp.data.model

data class DriverLocation(
    val driverId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val speed: Double = 0.0,
    val heading: Double = 0.0,
    val updatedAt: Long = 0L
)