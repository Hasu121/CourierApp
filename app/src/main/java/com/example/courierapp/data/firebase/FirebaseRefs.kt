package com.example.courierapp.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseRefs {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    const val USERS = "users"
    const val CUSTOMER_PROFILES = "customer_profiles"
    const val DRIVER_PROFILES = "driver_profiles"
    const val BOOKINGS = "bookings"
    const val BOOKING_STATUS_LOGS = "booking_status"
    const val NOTIFICATIONS = "notifications"
    const val DRIVER_LOCATIONS = "driver_locations"
    const val PAYMENTS = "payments"
    const val REVIEWS = "reviews"
    const val VEHICLES = "vehicles"
    const val ADMIN_SETTINGS = "admin_settings"
}