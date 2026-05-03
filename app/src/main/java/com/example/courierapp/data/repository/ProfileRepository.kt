package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.User

class ProfileRepository {

    fun getCurrentUserProfile(
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
                    onFailure("User profile not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load profile")
            }
    }

    fun updateCustomerProfile(
        fullName: String,
        phone: String,
        address: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val updates = mapOf(
            "fullName" to fullName,
            "phone" to phone,
            "address" to address,
            "updatedAt" to System.currentTimeMillis()
        )

        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .document(currentUser.uid)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update customer profile")
            }
    }

    fun updateDriverProfile(
        fullName: String,
        phone: String,
        address: String,
        vehicleTypes: List<String>,
        vehicleNumber: String,
        serviceMode: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val now = System.currentTimeMillis()

        val userUpdates = mapOf(
            "fullName" to fullName,
            "phone" to phone,
            "address" to address,
            "updatedAt" to now
        )

        val driverUpdates = mapOf(
            "vehicleTypes" to vehicleTypes,
            "vehicleNumber" to vehicleNumber,
            "serviceMode" to serviceMode,
            "updatedAt" to now
        )

        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .document(currentUser.uid)
            .update(userUpdates)
            .addOnSuccessListener {
                FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
                    .document(currentUser.uid)
                    .update(driverUpdates)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to update driver profile")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update user profile")
            }
    }
}