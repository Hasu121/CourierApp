package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.CustomerProfile
import com.example.courierapp.data.model.DriverProfile
import com.example.courierapp.data.model.User
import com.example.courierapp.utils.Constants

class AuthRepository {

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrEmpty()) {
                    onFailure("User ID not found")
                    return@addOnSuccessListener
                }

                FirebaseRefs.db.collection(FirebaseRefs.USERS).document(uid).get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            onFailure("User data not found")
                            return@addOnSuccessListener
                        }

                        val role = document.getString("role").orEmpty()
                        if (role.isEmpty()) {
                            onFailure("User role not found")
                            return@addOnSuccessListener
                        }

                        onSuccess(uid, role)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to load user data")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Login failed")
            }
    }

    fun registerCustomer(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrEmpty()) {
                    onFailure("User ID not found")
                    return@addOnSuccessListener
                }

                val now = System.currentTimeMillis()

                val user = User(
                    uid = uid,
                    role = Constants.ROLE_CUSTOMER,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    address = address,
                    photoUrl = "",
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )

                val customerProfile = CustomerProfile(
                    uid = uid,
                    defaultPickupAddress = "",
                    savedAddresses = emptyList(),
                    notes = "",
                    createdAt = now,
                    updatedAt = now
                )

                FirebaseRefs.db.collection(FirebaseRefs.USERS)
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        FirebaseRefs.db.collection(FirebaseRefs.CUSTOMER_PROFILES)
                            .document(uid)
                            .set(customerProfile)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Failed to save customer profile")
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to save user data")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Customer registration failed")
            }
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to send reset email")
            }
    }

    fun registerDriver(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String,
        vehicleTypes: List<String>,
        vehicleNumber: String,
        licenseNumber: String,
        nidNumber: String,
        serviceMode: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrEmpty()) {
                    onFailure("User ID not found")
                    return@addOnSuccessListener
                }

                val now = System.currentTimeMillis()

                val user = User(
                    uid = uid,
                    role = Constants.ROLE_DRIVER,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    address = address,
                    photoUrl = "",
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )

                val driverProfile = DriverProfile(
                    uid = uid,
                    vehicleTypes = vehicleTypes,
                    vehicleNumber = vehicleNumber,
                    licenseNumber = licenseNumber,
                    nidNumber = nidNumber,
                    serviceMode = serviceMode,
                    licensePhotoUrl = "",
                    vehiclePhotoUrl = "",
                    verificationStatus = "pending",
                    isAvailable = false,
                    rating = 0.0,
                    completedDeliveries = 0,
                    createdAt = now,
                    updatedAt = now
                )

                FirebaseRefs.db.collection(FirebaseRefs.USERS)
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
                            .document(uid)
                            .set(driverProfile)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Failed to save driver profile")
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to save user data")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Driver registration failed")
            }
    }
}