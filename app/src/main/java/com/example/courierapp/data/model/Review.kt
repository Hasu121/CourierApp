package com.example.courierapp.data.model

data class Review(
    val bookingId: String = "",
    val customerId: String = "",
    val driverId: String = "",

    val customerRatingForDriver: Int = 0,
    val customerCommentForDriver: String = "",
    val customerReviewed: Boolean = false,

    val driverRatingForCustomer: Int = 0,
    val driverCommentForCustomer: String = "",
    val driverReviewed: Boolean = false,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)