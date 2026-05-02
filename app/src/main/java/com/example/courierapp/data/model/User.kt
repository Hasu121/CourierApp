package com.example.courierapp.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = "",
    val photoUrl: String = "",
    val isActive: Boolean = true,
    val cancelPenaltyPending: Boolean = false,
    val cancelPenaltyAmount: Double = 0.0,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)