package com.example.courierapp.data.model

data class AdminDriverItem(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val vehicleType: String = "",
    val vehicleNumber: String = "",
    val licenseNumber: String = "",
    val serviceMode: List<String> = emptyList(),
    val verificationStatus: String = "pending"
)