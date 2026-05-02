package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.model.DriverLocation
import com.example.courierapp.data.model.DriverProfile
import com.example.courierapp.data.model.Earning
import com.example.courierapp.data.model.Penalty
import com.example.courierapp.data.model.User
import com.example.courierapp.utils.Constants
import com.google.firebase.firestore.DocumentSnapshot

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
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure("Driver user data not found")
                }
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
                if (profile != null) {
                    onSuccess(profile)
                } else {
                    onFailure("Driver profile not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load driver profile")
            }
    }

    fun getDriverHomeStatus(
        onSuccess: (
            isAvailable: Boolean,
            lat: Double,
            lng: Double,
            updatedAt: Long
        ) -> Unit,
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
                val isAvailable = profileDoc.getBoolean("isAvailable") ?: false

                FirebaseRefs.db.collection(FirebaseRefs.DRIVER_LOCATIONS)
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { locationDoc ->
                        val lat = locationDoc.getDouble("lat") ?: 0.0
                        val lng = locationDoc.getDouble("lng") ?: 0.0
                        val updatedAt = locationDoc.getLong("updatedAt") ?: 0L

                        onSuccess(isAvailable, lat, lng, updatedAt)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to load driver location")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load driver status")
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

    fun updateDriverAvailability(
        isAvailable: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        updateAvailability(isAvailable, onSuccess, onFailure)
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

                val verificationStatus = profileDoc.getString("verificationStatus") ?: "pending"
                if (verificationStatus != "verified") {
                    onFailure("Your driver account is not verified yet")
                    return@addOnSuccessListener
                }

                val serviceMode = profileDoc.get("serviceMode") as? List<*> ?: emptyList<String>()
                val driverModes = serviceMode.mapNotNull { it?.toString() }

                FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
                    .whereEqualTo("status", Constants.STATUS_PENDING)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val bookings = querySnapshot.documents.mapNotNull { doc ->
                            mapBookingDocument(doc)
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

                if (currentStatus != Constants.STATUS_PENDING || assignedDriverId.isNotEmpty()) {
                    onFailure("This job is no longer available")
                    return@addOnSuccessListener
                }

                val bookingUpdates = mapOf(
                    "assignedDriverId" to driverId,
                    "status" to Constants.STATUS_ACCEPTED,
                    "updatedAt" to now
                )

                val statusLog = mapOf(
                    "bookingId" to bookingId,
                    "status" to Constants.STATUS_ACCEPTED,
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
                    .addOnSuccessListener { onSuccess() }
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
                    mapBookingDocument(doc)
                }.filter { booking ->
                    booking.status != Constants.STATUS_DELIVERED &&
                            booking.status != Constants.STATUS_CANCELLED
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

        val finalFare = if (newStatus == Constants.STATUS_DELIVERED) {
            booking.estimatedFare
        } else {
            booking.finalFare
        }

        val bookingUpdates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to now
        )

        if (newStatus == Constants.STATUS_DELIVERED) {
            bookingUpdates["paymentMethod"] = Constants.PAYMENT_CASH
            bookingUpdates["paymentStatus"] = Constants.PAYMENT_PAID
            bookingUpdates["finalFare"] = finalFare
        }

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
                                if (newStatus == Constants.STATUS_DELIVERED) {
                                    createEarningForDeliveredBooking(
                                        booking = booking,
                                        amount = finalFare,
                                        createdAt = now,
                                        onSuccess = onSuccess,
                                        onFailure = onFailure
                                    )
                                } else {
                                    onSuccess()
                                }
                            }
                            .addOnFailureListener {
                                if (newStatus == Constants.STATUS_DELIVERED) {
                                    createEarningForDeliveredBooking(
                                        booking = booking,
                                        amount = finalFare,
                                        createdAt = now,
                                        onSuccess = onSuccess,
                                        onFailure = onFailure
                                    )
                                } else {
                                    onSuccess()
                                }
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

    private fun createEarningForDeliveredBooking(
        booking: Booking,
        amount: Double,
        createdAt: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.USERS)
            .document(booking.customerId)
            .get()
            .addOnSuccessListener { customerDoc ->
                val customerName = customerDoc.getString("fullName").orEmpty()

                val earning = Earning(
                    earningId = booking.bookingId,
                    driverId = currentUser.uid,
                    bookingId = booking.bookingId,
                    customerId = booking.customerId,
                    customerName = customerName,
                    dropAddress = booking.dropAddress,
                    amount = amount,
                    paymentMethod = Constants.PAYMENT_CASH,
                    paymentStatus = Constants.PAYMENT_PAID,
                    createdAt = createdAt
                )

                FirebaseRefs.db.collection(FirebaseRefs.EARNINGS)
                    .document(booking.bookingId)
                    .set(earning)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Delivery completed but earning failed")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load customer info for earning")
            }
    }

    fun getDriverEarnings(
        onSuccess: (
            totalEarnings: Double,
            todayEarnings: Double,
            completedCount: Int,
            earnings: List<Earning>
        ) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.EARNINGS)
            .whereEqualTo("driverId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val earnings = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Earning(
                            earningId = doc.getString("earningId").orEmpty(),
                            driverId = doc.getString("driverId").orEmpty(),
                            bookingId = doc.getString("bookingId").orEmpty(),
                            customerId = doc.getString("customerId").orEmpty(),
                            customerName = doc.getString("customerName").orEmpty(),
                            dropAddress = doc.getString("dropAddress").orEmpty(),
                            amount = doc.getDouble("amount") ?: 0.0,
                            paymentMethod = doc.getString("paymentMethod") ?: Constants.PAYMENT_CASH,
                            paymentStatus = doc.getString("paymentStatus") ?: Constants.PAYMENT_PAID,
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }

                val totalEarnings = earnings.sumOf { it.amount }
                val startOfToday = getStartOfTodayMillis()
                val todayEarnings = earnings
                    .filter { it.createdAt >= startOfToday }
                    .sumOf { it.amount }

                val completedCount = earnings.size

                onSuccess(totalEarnings, todayEarnings, completedCount, earnings)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load earnings")
            }
    }

    fun getDeliveryHistory(
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
            .whereEqualTo("status", Constants.STATUS_DELIVERED)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val deliveries = querySnapshot.documents.mapNotNull { doc ->
                    mapBookingDocument(doc)
                }.sortedByDescending { it.updatedAt }

                onSuccess(deliveries)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load delivery history")
            }
    }

    fun getCustomerNamesForBookings(
        bookings: List<Booking>,
        onSuccess: (Map<String, String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val customerIds = bookings.map { it.customerId }
            .distinct()
            .filter { it.isNotEmpty() }

        if (customerIds.isEmpty()) {
            onSuccess(emptyMap())
            return
        }

        val resultMap = mutableMapOf<String, String>()
        var completedRequests = 0

        customerIds.forEach { customerId ->
            FirebaseRefs.db.collection(FirebaseRefs.USERS)
                .document(customerId)
                .get()
                .addOnSuccessListener { document ->
                    resultMap[customerId] =
                        document.getString("fullName") ?: "Unknown Customer"

                    completedRequests++
                    if (completedRequests == customerIds.size) {
                        onSuccess(resultMap)
                    }
                }
                .addOnFailureListener {
                    resultMap[customerId] = "Unknown Customer"

                    completedRequests++
                    if (completedRequests == customerIds.size) {
                        onSuccess(resultMap)
                    }
                }
        }
    }

    fun rejectJob(
        booking: Booking,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        if (booking.status != Constants.STATUS_ACCEPTED) {
            onFailure("You can only reject before pickup")
            return
        }

        if (booking.assignedDriverId != currentUser.uid) {
            onFailure("This job is not assigned to you")
            return
        }

        val now = System.currentTimeMillis()
        val penaltyId = FirebaseRefs.db.collection(FirebaseRefs.PENALTIES).document().id

        val bookingUpdates = mapOf(
            "assignedDriverId" to "",
            "status" to Constants.STATUS_PENDING,
            "rejectedBy" to currentUser.uid,
            "driverPenaltyApplied" to Constants.DRIVER_REJECT_PENALTY,
            "updatedAt" to now
        )

        val statusLog = mapOf(
            "bookingId" to booking.bookingId,
            "status" to Constants.STATUS_REJECTED,
            "changedBy" to currentUser.uid,
            "note" to "Driver rejected the booking before pickup. Job returned to available list.",
            "timestamp" to now
        )

        val penalty = Penalty(
            penaltyId = penaltyId,
            userId = currentUser.uid,
            role = Constants.ROLE_DRIVER,
            bookingId = booking.bookingId,
            amount = Constants.DRIVER_REJECT_PENALTY,
            reason = "driver_rejected_before_pickup",
            createdAt = now
        )

        val notification = mapOf(
            "userId" to booking.customerId,
            "title" to "Driver Rejected Booking",
            "body" to "Your booking is available for another driver again.",
            "type" to "booking_update",
            "relatedBookingId" to booking.bookingId,
            "isRead" to false,
            "createdAt" to now
        )

        FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
            .document(booking.bookingId)
            .get()
            .addOnSuccessListener { document ->
                val latestStatus = document.getString("status").orEmpty()
                val latestAssignedDriverId = document.getString("assignedDriverId").orEmpty()

                if (
                    latestStatus != Constants.STATUS_ACCEPTED ||
                    latestAssignedDriverId != currentUser.uid
                ) {
                    onFailure("This job can no longer be rejected")
                    return@addOnSuccessListener
                }

                FirebaseRefs.db.collection(FirebaseRefs.BOOKINGS)
                    .document(booking.bookingId)
                    .update(bookingUpdates)
                    .addOnSuccessListener {
                        FirebaseRefs.db.collection(FirebaseRefs.BOOKING_STATUS_LOGS)
                            .add(statusLog)
                            .addOnSuccessListener {
                                FirebaseRefs.db.collection(FirebaseRefs.PENALTIES)
                                    .document(penaltyId)
                                    .set(penalty)
                                    .addOnSuccessListener {
                                        FirebaseRefs.db.collection(FirebaseRefs.NOTIFICATIONS)
                                            .add(notification)
                                            .addOnSuccessListener { onSuccess() }
                                            .addOnFailureListener { onSuccess() }
                                    }
                                    .addOnFailureListener { e ->
                                        onFailure(e.message ?: "Job rejected but penalty failed")
                                    }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Job rejected but log failed")
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to reject job")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to check job status")
            }
    }

    private fun mapBookingDocument(doc: DocumentSnapshot): Booking? {
        return try {
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

                paymentMethod = doc.getString("paymentMethod") ?: Constants.PAYMENT_CASH,
                paymentStatus = doc.getString("paymentStatus") ?: Constants.PAYMENT_PENDING,

                status = doc.getString("status") ?: Constants.STATUS_PENDING,
                cancelledBy = doc.getString("cancelledBy").orEmpty(),
                rejectedBy = doc.getString("rejectedBy").orEmpty(),
                cancelPenaltyApplied = doc.getDouble("cancelPenaltyApplied") ?: 0.0,
                driverPenaltyApplied = doc.getDouble("driverPenaltyApplied") ?: 0.0,

                createdAt = doc.getLong("createdAt") ?: 0L,
                updatedAt = doc.getLong("updatedAt") ?: 0L
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getStartOfTodayMillis(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}