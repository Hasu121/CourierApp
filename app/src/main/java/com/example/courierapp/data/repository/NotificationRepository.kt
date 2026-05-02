package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.NotificationItem
import com.google.firebase.firestore.Query

class NotificationRepository {

    fun getDriverNotifications(
        onSuccess: (List<NotificationItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        getCurrentUserNotifications(onSuccess, onFailure)
    }

    fun getCustomerNotifications(
        onSuccess: (List<NotificationItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        getCurrentUserNotifications(onSuccess, onFailure)
    }

    private fun getCurrentUserNotifications(
        onSuccess: (List<NotificationItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.NOTIFICATIONS)
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val notifications = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        NotificationItem(
                            notificationId = doc.id,
                            userId = doc.getString("userId").orEmpty(),
                            title = doc.getString("title").orEmpty(),
                            body = doc.getString("body").orEmpty(),
                            type = doc.getString("type").orEmpty(),
                            relatedBookingId = doc.getString("relatedBookingId").orEmpty(),
                            isRead = doc.getBoolean("isRead") ?: false,
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onSuccess(notifications)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load notifications")
            }
    }

    fun markNotificationAsRead(
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (notificationId.isEmpty()) {
            onFailure("Invalid notification")
            return
        }

        FirebaseRefs.db.collection(FirebaseRefs.NOTIFICATIONS)
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to mark notification as read")
            }
    }
}