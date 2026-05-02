package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.DriverLocation
import com.example.courierapp.data.model.DriverProfile
import com.example.courierapp.data.model.User
import com.example.courierapp.data.model.Booking


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

    fun getAvailableJobs(
        onSuccess: (List<Booking>) -> Unit,
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
            .addOnSuccessListener { profileDoc ->
                val serviceMode = profileDoc.get("serviceMode") as? List<*> ?: emptyList<String>()
                val driverModes = serviceMode.mapNotNull { it?.toString() }

                FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val bookings = querySnapshot.documents.mapNotNull {
                            it.toObject(com.example.courierapp.data.model.Booking::class.java)
                        }

                        val filteredJobs = bookings.filter { booking ->
                            booking.assignedDriverId.isEmpty() &&
                                    driverModes.contains(booking.bookingType)
                        }

                        onSuccess(filteredJobs)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to load jobs")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load driver profile")
            }
    }

    fun acceptJob(
        bookingId: String,
        customerId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val driverId = currentUser.uid
        val now = System.currentTimeMillis()

        val bookingRef = FirebaseRefs.db
            .collection(FirebaseRefs.BOOKINGS)
            .document(bookingId)

        val logRef = FirebaseRefs.db
            .collection(FirebaseRefs.BOOKING_STATUS_LOGS)
            .document()

        val notificationRef = FirebaseRefs.db
            .collection(FirebaseRefs.NOTIFICATIONS)
            .document()

        bookingRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onFailure("Booking not found")
                    return@addOnSuccessListener
                }

                val currentStatus = document.getString("status").orEmpty()
                val assignedDriverId = document.getString("assignedDriverId").orEmpty()

                if (currentStatus != "pending" || assignedDriverId.isNotEmpty()) {
                    onFailure("This job is no longer available")
                    return@addOnSuccessListener
                }

                val bookingUpdates = mapOf(
                    "assignedDriverId" to driverId,
                    "status" to "accepted",
                    "updatedAt" to now
                )

                val statusLog = mapOf(
                    "bookingId" to bookingId,
                    "status" to "accepted",
                    "changedBy" to driverId,
                    "note" to "Driver accepted the booking",
                    "timestamp" to now
                )

                val notification = mapOf(
                    "userId" to customerId,
                    "title" to "Booking Accepted",
                    "body" to "A driver has accepted your courier booking.",
                    "type" to "booking_update",
                    "relatedBookingId" to bookingId,
                    "isRead" to false,
                    "createdAt" to now
                )

                val batch = FirebaseRefs.db.batch()

                batch.update(bookingRef, bookingUpdates)
                batch.set(logRef, statusLog)
                batch.set(notificationRef, notification)

                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to accept job")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to check job availability")
            }
    }

    fun getMyDeliveries(
        onSuccess: (List<Booking>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .whereEqualTo("assignedDriverId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val deliveries = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Booking(
                            bookingId = doc.getString("bookingId").orEmpty(),
                            customerId = doc.getString("customerId").orEmpty(),
                            assignedDriverId = doc.getString("assignedDriverId").orEmpty(),
                            bookingType = doc.getString("bookingType").orEmpty(),
                            pickupAddress = doc.getString("pickupAddress").orEmpty(),
                            pickupLat = doc.getDouble("pickupLat") ?: 0.0,
                            pickupLng = doc.getDouble("pickupLng") ?: 0.0,
                            dropAddress = doc.getString("dropAddress").orEmpty(),
                            dropLat = doc.getDouble("dropLat") ?: 0.0,
                            dropLng = doc.getDouble("dropLng") ?: 0.0,
                            distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                            packageType = doc.getString("packageType").orEmpty(),
                            packageWeight = doc.getString("packageWeight").orEmpty(),
                            packageNote = doc.getString("packageNote").orEmpty(),
                            receiverName = doc.getString("receiverName").orEmpty(),
                            receiverPhone = doc.getString("receiverPhone").orEmpty(),
                            preferredTime = doc.getString("preferredTime").orEmpty(),
                            estimatedFare = doc.getDouble("estimatedFare") ?: 0.0,
                            finalFare = doc.getDouble("finalFare") ?: 0.0,
                            paymentMethod = doc.getString("paymentMethod") ?: "cash",
                            paymentStatus = doc.getString("paymentStatus") ?: "pending",
                            status = doc.getString("status") ?: "pending",
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.filter { booking ->
                    booking.status != "delivered" && booking.status != "cancelled"
                }.sortedByDescending { it.updatedAt }

                onSuccess(deliveries)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load deliveries")
            }
    }

    fun updateDeliveryStatus(
        booking: Booking,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val now = System.currentTimeMillis()

        val bookingUpdates = mapOf(
            "status" to newStatus,
            "updatedAt" to now
        )

        val statusLog = mapOf(
            "bookingId" to booking.bookingId,
            "status" to newStatus,
            "changedBy" to currentUser.uid,
            "note" to "Delivery status changed to $newStatus",
            "timestamp" to now
        )

        val notification = mapOf(
            "userId" to booking.customerId,
            "title" to "Delivery Status Updated",
            "body" to "Your booking is now $newStatus.",
            "type" to "booking_update",
            "relatedBookingId" to booking.bookingId,
            "isRead" to false,
            "createdAt" to now
        )

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .document(booking.bookingId)
            .update(bookingUpdates)
            .addOnSuccessListener {
                FirebaseRefs.db.collection(FirebaseRefs.BOOKING_STATUS_LOGS)
                    .add(statusLog)
                    .addOnSuccessListener {
                        FirebaseRefs.db.collection(FirebaseRefs.NOTIFICATIONS)
                            .add(notification)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onSuccess()
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Status updated but log failed")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update delivery status")
            }
    }
}