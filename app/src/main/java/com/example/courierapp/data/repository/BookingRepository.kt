package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.model.BookingStatusLog
import com.example.courierapp.utils.Constants

class BookingRepository {

    fun createBooking(
        bookingType: String,
        pickupAddress: String,
        dropAddress: String,
        packageType: String,
        packageWeight: String,
        packageNote: String,
        receiverName: String,
        receiverPhone: String,
        preferredTime: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val bookingId = FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS).document().id
        val now = System.currentTimeMillis()

        val booking = Booking(
            bookingId = bookingId,
            customerId = currentUser.uid,
            assignedDriverId = "",
            bookingType = bookingType,
            pickupAddress = pickupAddress,
            pickupLat = 0.0,
            pickupLng = 0.0,
            dropAddress = dropAddress,
            dropLat = 0.0,
            dropLng = 0.0,
            packageType = packageType,
            packageWeight = packageWeight,
            packageNote = packageNote,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            preferredTime = preferredTime,
            estimatedFare = 0.0,
            finalFare = 0.0,
            paymentMethod = "cash",
            paymentStatus = "pending",
            status = Constants.STATUS_PENDING,
            createdAt = now,
            updatedAt = now
        )

        val bookingLog = BookingStatusLog(
            bookingId = bookingId,
            status = Constants.STATUS_PENDING,
            changedBy = currentUser.uid,
            note = "Booking created",
            timestamp = now
        )

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .document(bookingId)
            .set(booking)
            .addOnSuccessListener {
                FirebaseRefs.db.collection(FirebaseRefs.BOOKING_STATUS_LOGS)
                    .add(bookingLog)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Booking saved but status log failed")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to create booking")
            }
    }
}