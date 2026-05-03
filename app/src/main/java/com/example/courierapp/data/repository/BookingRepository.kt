package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.model.BookingStatusLog
import com.example.courierapp.utils.Constants

class BookingRepository {

    fun createBooking(
        bookingType: String,
        vehicleType: String,
        pickupAddress: String,
        pickupLat: Double,
        pickupLng: Double,
        dropAddress: String,
        dropLat: Double,
        dropLng: Double,
        packageType: String,
        packageWeight: String,
        packageNote: String,
        receiverName: String,
        receiverPhone: String,
        preferredTime: String,
        distanceKm: Double,
        estimatedFare: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val penaltyPending = userDoc.getBoolean("cancelPenaltyPending") ?: false

                val penaltyAmount = if (penaltyPending) {
                    userDoc.getDouble("cancelPenaltyAmount") ?: 0.0
                } else {
                    0.0
                }

                val finalEstimatedFare = estimatedFare + penaltyAmount

                saveBookingAfterPenaltyCheck(
                    customerId = currentUser.uid,
                    bookingType = bookingType,
                    pickupAddress = pickupAddress,
                    pickupLat = pickupLat,
                    pickupLng = pickupLng,
                    vehicleType = vehicleType,
                    dropAddress = dropAddress,
                    dropLat = dropLat,
                    dropLng = dropLng,
                    packageType = packageType,
                    packageWeight = packageWeight,
                    packageNote = packageNote,
                    receiverName = receiverName,
                    receiverPhone = receiverPhone,
                    preferredTime = preferredTime,
                    distanceKm = distanceKm,
                    estimatedFare = finalEstimatedFare,
                    cancelPenaltyApplied = penaltyAmount,
                    clearPenaltyAfterSave = penaltyPending,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to check customer penalty")
            }
    }

    private fun saveBookingAfterPenaltyCheck(
        customerId: String,
        bookingType: String,
        vehicleType: String,
        pickupAddress: String,
        pickupLat: Double,
        pickupLng: Double,
        dropAddress: String,
        dropLat: Double,
        dropLng: Double,
        packageType: String,
        packageWeight: String,
        packageNote: String,
        receiverName: String,
        receiverPhone: String,
        preferredTime: String,
        distanceKm: Double,
        estimatedFare: Double,
        cancelPenaltyApplied: Double,
        clearPenaltyAfterSave: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val bookingId = FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS).document().id
        val now = System.currentTimeMillis()

        val booking = Booking(
            bookingId = bookingId,
            customerId = customerId,
            assignedDriverId = "",
            bookingType = bookingType,
            vehicleType = vehicleType,
            pickupAddress = pickupAddress,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropAddress = dropAddress,
            dropLat = dropLat,
            dropLng = dropLng,
            distanceKm = distanceKm,
            packageType = packageType,
            packageWeight = packageWeight,
            packageNote = packageNote,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            rejectedBy = "",
            driverPenaltyApplied = 0.0,
            preferredTime = preferredTime,
            estimatedFare = estimatedFare,
            finalFare = 0.0,
            paymentMethod = Constants.PAYMENT_CASH,
            paymentStatus = Constants.PAYMENT_PENDING,
            status = Constants.STATUS_PENDING,
            cancelledBy = "",
            cancelPenaltyApplied = cancelPenaltyApplied,
            createdAt = now,
            updatedAt = now
        )

        val bookingLog = BookingStatusLog(
            bookingId = bookingId,
            status = Constants.STATUS_PENDING,
            changedBy = customerId,
            note = if (cancelPenaltyApplied > 0.0) {
                "Booking created with cancellation penalty applied"
            } else {
                "Booking created"
            },
            timestamp = now
        )

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .document(bookingId)
            .set(booking)
            .addOnSuccessListener {
                FirebaseRefs.db.collection(FirebaseRefs.BOOKING_STATUS_LOGS)
                    .add(bookingLog)
                    .addOnSuccessListener {
                        if (clearPenaltyAfterSave) {
                            clearCustomerPenalty(
                                customerId = customerId,
                                updatedAt = now,
                                onSuccess = onSuccess,
                                onFailure = onFailure
                            )
                        } else {
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Booking saved but status log failed")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to create booking")
            }
    }

    private fun clearCustomerPenalty(
        customerId: String,
        updatedAt: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .document(customerId)
            .update(
                mapOf(
                    "cancelPenaltyPending" to false,
                    "cancelPenaltyAmount" to 0.0,
                    "updatedAt" to updatedAt
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Booking saved but penalty reset failed")
            }
    }
}