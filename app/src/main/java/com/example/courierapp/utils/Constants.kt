package com.example.courierapp.utils

object Constants {
    const val ROLE_CUSTOMER = "customer"
    const val ROLE_DRIVER = "driver"
    const val ROLE_ADMIN = "admin"

    const val STATUS_PENDING = "pending"
    const val STATUS_ACCEPTED = "accepted"
    const val STATUS_PICKED_UP = "picked_up"
    const val STATUS_IN_TRANSIT = "in_transit"
    const val STATUS_DELIVERED = "delivered"
    const val STATUS_CANCELLED = "cancelled"
    const val STATUS_REJECTED = "rejected"

    const val PAYMENT_CASH = "cash"
    const val PAYMENT_PENDING = "pending"
    const val PAYMENT_PAID = "paid"

    const val EARNING_TYPE_DELIVERY = "delivery_earning"

    const val CUSTOMER_CANCEL_PENALTY = 50.0
    const val DRIVER_REJECT_PENALTY = 50.0

    // Booking / service labels
    const val BOOKING_WITHIN_CITY = "Within City"
    const val BOOKING_INTERCITY = "Intercity"

    const val SERVICE_WITHIN_CITY = "Within City"
    const val SERVICE_INTERCITY = "Intercity"
    const val SERVICE_BOTH = "Both"

    // Vehicle labels
    const val VEHICLE_BIKE = "Bike"
    const val VEHICLE_CAR = "Car"
    const val VEHICLE_TRUCK = "Truck"

    // Package weight labels
    const val WEIGHT_LIGHT = "Light"
    const val WEIGHT_MEDIUM = "Medium"
    const val WEIGHT_HEAVY = "Heavy"
}

object FareConfig {
    const val WITHIN_CITY_BASE_FARE = 80.0
    const val INTERCITY_BASE_FARE = 250.0

    const val PER_KM_BIKE = 12.0
    const val PER_KM_CAR = 18.0
    const val PER_KM_TRUCK = 30.0

    const val WEIGHT_LIGHT = 0.0
    const val WEIGHT_MEDIUM = 30.0
    const val WEIGHT_HEAVY = 60.0
}