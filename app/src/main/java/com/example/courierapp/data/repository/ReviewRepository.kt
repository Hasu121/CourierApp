package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.model.Review
import com.example.courierapp.utils.Constants

class ReviewRepository {

    fun submitCustomerReviewForDriver(
        booking: Booking,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (booking.status != Constants.STATUS_DELIVERED) {
            onFailure("You can review only after delivery")
            return
        }

        if (booking.assignedDriverId.isEmpty()) {
            onFailure("No driver assigned")
            return
        }

        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null || currentUser.uid != booking.customerId) {
            onFailure("Invalid customer")
            return
        }

        val now = System.currentTimeMillis()
        val reviewRef = FirebaseRefs.db.collection(FirebaseRefs.REVIEWS).document(booking.bookingId)

        reviewRef.get()
            .addOnSuccessListener { doc ->
                val alreadyReviewed = doc.getBoolean("customerReviewed") ?: false
                if (alreadyReviewed) {
                    onFailure("You already reviewed this driver")
                    return@addOnSuccessListener
                }

                val updates = if (doc.exists()) {
                    mapOf(
                        "customerRatingForDriver" to rating,
                        "customerCommentForDriver" to comment,
                        "customerReviewed" to true,
                        "updatedAt" to now
                    )
                } else {
                    val review = Review(
                        bookingId = booking.bookingId,
                        customerId = booking.customerId,
                        driverId = booking.assignedDriverId,
                        customerRatingForDriver = rating,
                        customerCommentForDriver = comment,
                        customerReviewed = true,
                        driverRatingForCustomer = 0,
                        driverCommentForCustomer = "",
                        driverReviewed = false,
                        createdAt = now,
                        updatedAt = now
                    )
                    null
                }

                if (updates != null) {
                    reviewRef.update(updates)
                        .addOnSuccessListener {
                            updateDriverAverageRating(booking.assignedDriverId, rating, onSuccess, onFailure)
                        }
                        .addOnFailureListener { e ->
                            onFailure(e.message ?: "Failed to save review")
                        }
                } else {
                    val review = Review(
                        bookingId = booking.bookingId,
                        customerId = booking.customerId,
                        driverId = booking.assignedDriverId,
                        customerRatingForDriver = rating,
                        customerCommentForDriver = comment,
                        customerReviewed = true,
                        driverRatingForCustomer = 0,
                        driverCommentForCustomer = "",
                        driverReviewed = false,
                        createdAt = now,
                        updatedAt = now
                    )

                    reviewRef.set(review)
                        .addOnSuccessListener {
                            updateDriverAverageRating(booking.assignedDriverId, rating, onSuccess, onFailure)
                        }
                        .addOnFailureListener { e ->
                            onFailure(e.message ?: "Failed to save review")
                        }
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to check review")
            }
    }

    fun submitDriverReviewForCustomer(
        booking: Booking,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (booking.status != Constants.STATUS_DELIVERED) {
            onFailure("You can review only after delivery")
            return
        }

        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null || currentUser.uid != booking.assignedDriverId) {
            onFailure("Invalid driver")
            return
        }

        val now = System.currentTimeMillis()
        val reviewRef = FirebaseRefs.db.collection(FirebaseRefs.REVIEWS).document(booking.bookingId)

        reviewRef.get()
            .addOnSuccessListener { doc ->
                val alreadyReviewed = doc.getBoolean("driverReviewed") ?: false
                if (alreadyReviewed) {
                    onFailure("You already reviewed this customer")
                    return@addOnSuccessListener
                }

                if (doc.exists()) {
                    val updates = mapOf(
                        "driverRatingForCustomer" to rating,
                        "driverCommentForCustomer" to comment,
                        "driverReviewed" to true,
                        "updatedAt" to now
                    )

                    reviewRef.update(updates)
                        .addOnSuccessListener {
                            updateCustomerAverageRating(booking.customerId, rating, onSuccess, onFailure)
                        }
                        .addOnFailureListener { e ->
                            onFailure(e.message ?: "Failed to save review")
                        }
                } else {
                    val review = Review(
                        bookingId = booking.bookingId,
                        customerId = booking.customerId,
                        driverId = booking.assignedDriverId,
                        customerRatingForDriver = 0,
                        customerCommentForDriver = "",
                        customerReviewed = false,
                        driverRatingForCustomer = rating,
                        driverCommentForCustomer = comment,
                        driverReviewed = true,
                        createdAt = now,
                        updatedAt = now
                    )

                    reviewRef.set(review)
                        .addOnSuccessListener {
                            updateCustomerAverageRating(booking.customerId, rating, onSuccess, onFailure)
                        }
                        .addOnFailureListener { e ->
                            onFailure(e.message ?: "Failed to save review")
                        }
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to check review")
            }
    }

    fun checkCustomerReviewed(
        bookingId: String,
        onResult: (Boolean) -> Unit
    ) {
        FirebaseRefs.db.collection(FirebaseRefs.REVIEWS)
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                val reviewed = document.getBoolean("customerReviewed") ?: false
                onResult(reviewed)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun checkDriverReviewed(
        bookingId: String,
        onResult: (Boolean) -> Unit
    ) {
        FirebaseRefs.db.collection(FirebaseRefs.REVIEWS)
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                val reviewed = document.getBoolean("driverReviewed") ?: false
                onResult(reviewed)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    private fun updateDriverAverageRating(
        driverId: String,
        newRating: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val driverRef = FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES).document(driverId)

        driverRef.get()
            .addOnSuccessListener { doc ->
                val oldAverage = doc.getDouble("ratingAverage") ?: doc.getDouble("rating") ?: 0.0
                val oldCount = doc.getLong("ratingCount")?.toInt() ?: 0

                val newCount = oldCount + 1
                val newAverage = ((oldAverage * oldCount) + newRating) / newCount

                driverRef.update(
                    mapOf(
                        "ratingAverage" to newAverage,
                        "ratingCount" to newCount,
                        "rating" to newAverage,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e.message ?: "Failed to update driver rating") }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load driver rating")
            }
    }

    private fun updateCustomerAverageRating(
        customerId: String,
        newRating: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userRef = FirebaseRefs.db.collection(FirebaseRefs.USERS).document(customerId)

        userRef.get()
            .addOnSuccessListener { doc ->
                val oldAverage = doc.getDouble("ratingAverage") ?: 0.0
                val oldCount = doc.getLong("ratingCount")?.toInt() ?: 0

                val newCount = oldCount + 1
                val newAverage = ((oldAverage * oldCount) + newRating) / newCount

                userRef.update(
                    mapOf(
                        "ratingAverage" to newAverage,
                        "ratingCount" to newCount,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e.message ?: "Failed to update customer rating") }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load customer rating")
            }
    }
}