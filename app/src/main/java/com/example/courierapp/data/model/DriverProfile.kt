package com.example.courierapp.data.model

data class DriverProfile(
    val uid: String = "",
    val vehicleType: String = "",
    val vehicleNumber: String = "",
    val licenseNumber: String = "",
    val nidNumber: String = "",
    val serviceMode: List<String> = emptyList(),
    val licensePhotoUrl: String = "",
    val vehiclePhotoUrl: String = "",
    val verificationStatus: String = "pending",
    val isAvailable: Boolean = false,
    val rating: Double = 0.0,
    val completedDeliveries: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)