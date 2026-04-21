package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.DriverLocation
import com.example.courierapp.data.model.DriverProfile
import com.example.courierapp.data.model.User

class DriverRepository {

    fun getCurrentDriverUser(
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
                if (user != null) onSuccess(user) else onFailure("Driver user data not found")
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load driver user")
            }
    }

    fun getCurrentDriverProfile(
        onSuccess: (DriverProfile) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(DriverProfile::class.java)
                if (profile != null) onSuccess(profile) else onFailure("Driver profile not found")
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load driver profile")
            }
    }

    fun updateAvailability(
        isAvailable: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
            .document(currentUser.uid)
            .update(
                mapOf(
                    "isAvailable" to isAvailable,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update availability")
            }
    }

    fun updateDriverLocation(
        lat: Double,
        lng: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val now = System.currentTimeMillis()

        val location = DriverLocation(
            driverId = currentUser.uid,
            lat = lat,
            lng = lng,
            speed = 0.0,
            heading = 0.0,
            updatedAt = now
        )

        FirebaseRefs.db.collection(FirebaseRefs.DRIVER_LOCATIONS)
            .document(currentUser.uid)
            .set(location)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update location")
            }
    }
}