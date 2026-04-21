package com.example.courierapp.utils

import android.location.Location
import kotlin.math.ceil

object FareCalculator {

    fun calculateDistanceKm(
        pickupLat: Double,
        pickupLng: Double,
        dropLat: Double,
        dropLng: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            pickupLat,
            pickupLng,
            dropLat,
            dropLng,
            results
        )
        val meters = results[0].toDouble()
        return meters / 1000.0
    }

    fun calculateEstimatedFare(
        bookingType: String,
        packageWeight: String,
        pickupLat: Double,
        pickupLng: Double,
        dropLat: Double,
        dropLng: Double
    ): Pair<Double, Double> {
        val distanceKm = calculateDistanceKm(
            pickupLat,
            pickupLng,
            dropLat,
            dropLng
        )

        val baseFare = if (bookingType == "intercity") 250.0 else 80.0
        val perKmRate = if (bookingType == "intercity") 18.0 else 12.0

        val weightSurcharge = when (packageWeight) {
            "medium" -> 30.0
            "heavy" -> 60.0
            else -> 0.0
        }

        val rawFare = baseFare + (distanceKm * perKmRate) + weightSurcharge

        val roundedFare = ceil(rawFare / 10.0) * 10.0

        return Pair(distanceKm, roundedFare)
    }
}