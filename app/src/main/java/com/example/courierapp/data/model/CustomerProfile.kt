package com.example.courierapp.data.model

data class CustomerProfile(
    val uid: String = "",
    val defaultPickupAddress: String = "",
    val savedAddresses: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)