package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.model.User
import com.example.courierapp.data.model.BookingStatusLog
import com.example.courierapp.utils.Constants

class CustomerRepository {

    fun getCurrentCustomerProfile(
        onSuccess: (User) -> Unit,
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
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure("User data not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load user data")
            }
    }

    fun getRecentBookings(
        onSuccess: (List<Booking>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .whereEqualTo("customerId", currentUser.uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bookings = querySnapshot.documents.mapNotNull { it.toObject(Booking::class.java) }
                onSuccess(bookings)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load recent bookings")
            }
    }

    fun getAllCustomerBookings(
        onSuccess: (List<Booking>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .whereEqualTo("customerId", currentUser.uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bookings = querySnapshot.documents.mapNotNull { it.toObject(Booking::class.java) }
                onSuccess(bookings)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load booking history")
            }
    }

    fun getBookingById(
        bookingId: String,
        onSuccess: (Booking) -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                val booking = document.toObject(Booking::class.java)
                if (booking != null) {
                    onSuccess(booking)
                } else {
                    onFailure("Booking not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load booking details")
            }
    }

    fun getBookingStatusLogs(
        bookingId: String,
        onSuccess: (List<BookingStatusLog>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.db.collection(FirebaseRefs.BOOKING_STATUS_LOGS)
            .whereEqualTo("bookingId", bookingId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val logs = querySnapshot.documents.mapNotNull {
                    it.toObject(BookingStatusLog::class.java)
                }
                onSuccess(logs)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load booking status logs")
            }
    }

    fun getDriverLocation(
        driverId: String,
        onSuccess: (Double, Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (driverId.isEmpty()) {
            onFailure("Driver not assigned")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.DRIVER_LOCATIONS)
            .document(driverId)
            .get()
            .addOnSuccessListener { document ->
                val lat = document.getDouble("lat") ?: 0.0
                val lng = document.getDouble("lng") ?: 0.0

                if (lat == 0.0 && lng == 0.0) {
                    onFailure("Driver location not available")
                } else {
                    onSuccess(lat, lng)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load driver location")
            }
    }

    fun cancelBooking(
        booking: Booking,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val cancellableStatuses = listOf(
            Constants.STATUS_PENDING,
            Constants.STATUS_ACCEPTED
        )

        if (!cancellableStatuses.contains(booking.status)) {
            onFailure("This booking can no longer be cancelled")
            return
        }

        val now = System.currentTimeMillis()

        val bookingUpdates = mapOf(
            "status" to Constants.STATUS_CANCELLED,
            "cancelledBy" to currentUser.uid,
            "updatedAt" to now
        )

        val statusLog = mapOf(
            "bookingId" to booking.bookingId,
            "status" to Constants.STATUS_CANCELLED,
            "changedBy" to currentUser.uid,
            "note" to "Customer cancelled the booking",
            "timestamp" to now
        )

        val userPenaltyUpdates = mapOf(
            "cancelPenaltyPending" to true,
            "cancelPenaltyAmount" to Constants.CUSTOMER_CANCEL_PENALTY,
            "updatedAt" to now
        )

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .document(booking.bookingId)
            .update(bookingUpdates)
            .addOnSuccessListener {
                FirebaseRefs.db.collection(FirebaseRefs.BOOKING_STATUS_LOGS)
                    .add(statusLog)
                    .addOnSuccessListener {
                        FirebaseRefs.db.collection(FirebaseRefs.USERS)
                            .document(currentUser.uid)
                            .update(userPenaltyUpdates)
                            .addOnSuccessListener {
                                if (booking.assignedDriverId.isNotEmpty()) {
                                    createDriverCancellationNotification(
                                        driverId = booking.assignedDriverId,
                                        bookingId = booking.bookingId,
                                        onDone = onSuccess
                                    )
                                } else {
                                    onSuccess()
                                }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Booking cancelled but penalty update failed")
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Booking cancelled but log failed")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to cancel booking")
            }
    }

    private fun createDriverCancellationNotification(
        driverId: String,
        bookingId: String,
        onDone: () -> Unit
    ) {
        val now = System.currentTimeMillis()

        val notification = mapOf(
            "userId" to driverId,
            "title" to "Booking Cancelled",
            "body" to "A customer cancelled an assigned booking.",
            "type" to "booking_cancelled",
            "relatedBookingId" to bookingId,
            "isRead" to false,
            "createdAt" to now
        )

        FirebaseRefs.db.collection(FirebaseRefs.NOTIFICATIONS)
            .add(notification)
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onDone() }
    }

}