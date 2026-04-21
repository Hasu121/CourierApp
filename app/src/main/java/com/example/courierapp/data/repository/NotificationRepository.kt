package com.example.courierapp.data.repository

import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.model.NotificationItem
import com.google.firebase.firestore.Query

class NotificationRepository {

    fun getDriverNotifications(
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
                val notifications = querySnapshot.documents.mapNotNull {
                    it.toObject(NotificationItem::class.java)
                }
                onSuccess(notifications)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load notifications")
            }
    }
}