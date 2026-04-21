package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.model.User

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
}