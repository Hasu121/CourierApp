package com.example.courierapp.utils

object StatusFormatter {

    fun formatStatus(status: String): String {
        return when (status) {
            Constants.STATUS_PENDING -> "Pending"
            Constants.STATUS_ACCEPTED -> "Accepted"
            Constants.STATUS_PICKED_UP -> "Picked Up"
            Constants.STATUS_IN_TRANSIT -> "In Transit"
            Constants.STATUS_DELIVERED -> "Delivered"
            Constants.STATUS_CANCELLED -> "Cancelled"
            else -> status.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }
}