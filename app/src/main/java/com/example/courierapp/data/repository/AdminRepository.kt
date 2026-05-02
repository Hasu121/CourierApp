package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.AdminDriverItem

class AdminRepository {

    fun getAllDrivers(
        onSuccess: (List<AdminDriverItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .whereEqualTo("role", "driver")
            .get()
            .addOnSuccessListener { usersSnapshot ->
                val driverUsers = usersSnapshot.documents

                if (driverUsers.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val result = mutableListOf<AdminDriverItem>()
                var completed = 0

                driverUsers.forEach { userDoc ->
                    val uid = userDoc.getString("uid").orEmpty()

                    FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
                        .document(uid)
                        .get()
                        .addOnSuccessListener { profileDoc ->
                            val serviceModeRaw = profileDoc.get("serviceMode") as? List<*> ?: emptyList<String>()
                            val serviceMode = serviceModeRaw.mapNotNull { it?.toString() }

                            result.add(
                                AdminDriverItem(
                                    uid = uid,
                                    fullName = userDoc.getString("fullName").orEmpty(),
                                    email = userDoc.getString("email").orEmpty(),
                                    phone = userDoc.getString("phone").orEmpty(),
                                    vehicleType = profileDoc.getString("vehicleType").orEmpty(),
                                    vehicleNumber = profileDoc.getString("vehicleNumber").orEmpty(),
                                    licenseNumber = profileDoc.getString("licenseNumber").orEmpty(),
                                    serviceMode = serviceMode,
                                    verificationStatus = profileDoc.getString("verificationStatus") ?: "pending"
                                )
                            )

                            completed++
                            if (completed == driverUsers.size) {
                                onSuccess(result.sortedBy { it.verificationStatus })
                            }
                        }
                        .addOnFailureListener {
                            completed++
                            if (completed == driverUsers.size) {
                                onSuccess(result.sortedBy { it.verificationStatus })
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load drivers")
            }
    }

    fun updateDriverVerification(
        driverId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseRefs.db.collection(FirebaseRefs.DRIVER_PROFILES)
            .document(driverId)
            .update(
                mapOf(
                    "verificationStatus" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                createDriverVerificationNotification(driverId, status, onSuccess)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update driver verification")
            }
    }

    private fun createDriverVerificationNotification(
        driverId: String,
        status: String,
        onDone: () -> Unit
    ) {
        val title = when (status) {
            "verified" -> "Driver Account Approved"
            "rejected" -> "Driver Account Rejected"
            "pending" -> "Driver Account Set to Pending"
            else -> "Driver Verification Updated"
        }

        val body = when (status) {
            "verified" -> "Your driver account has been approved. You can now accept jobs."
            "rejected" -> "Your driver account has been rejected. Please contact admin."
            "pending" -> "Your driver account is pending verification again."
            else -> "Your driver account verification status was updated."
        }

        val notification = mapOf(
            "userId" to driverId,
            "title" to title,
            "body" to body,
            "type" to "driver_verification",
            "relatedBookingId" to "",
            "isRead" to false,
            "createdAt" to System.currentTimeMillis()
        )

        FirebaseRefs.db.collection(FirebaseRefs.NOTIFICATIONS)
            .add(notification)
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onDone() }
    }
}