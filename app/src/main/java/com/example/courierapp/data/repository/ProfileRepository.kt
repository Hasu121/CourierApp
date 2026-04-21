package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs

class ProfileRepository {

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
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update customer profile")
            }
    }

    fun updateDriverProfile(
        fullName: String,
        phone: String,
        address: String,
        vehicleType: String,
        vehicleNumber: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val userUpdates = mapOf(
            "fullName" to fullName,
            "phone" to phone,
            "address" to address,
            "updatedAt" to System.currentTimeMillis()
        )

        val driverUpdates = mapOf(
            "vehicleType" to vehicleType,
            "vehicleNumber" to vehicleNumber,
            "updatedAt" to System.currentTimeMillis()
        )

        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .document(currentUser.uid)
            .update(userUpdates)
            .addOnSuccessListener {
                FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
                    .document(currentUser.uid)
                    .update(driverUpdates)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to update driver profile")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update driver user info")
            }
    }
}