package com.example.courierapp.utils

object Constants {
    const val ROLE_CUSTOMER = "customer"
    const val ROLE_DRIVER = "driver"

    const val STATUS_PENDING = "pending"
    const val STATUS_ACCEPTED = "accepted"
    const val STATUS_PICKED_UP = "picked_up"
    const val STATUS_IN_TRANSIT = "in_transit"
    const val STATUS_DELIVERED = "delivered"
    const val STATUS_CANCELLED = "cancelled"
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