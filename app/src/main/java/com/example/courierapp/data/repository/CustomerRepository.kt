package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.model.User
import com.example.courierapp.data.model.BookingStatusLog

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

}