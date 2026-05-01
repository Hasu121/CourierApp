package com.example.courierapp.ui.customer.history

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.data.repository.CustomerRepository
import com.example.courierapp.databinding.ActivityBookingDetailsBinding
import com.example.courierapp.ui.common.BookingStatusLogAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailsBinding
    private val customerRepository = CustomerRepository()
    private lateinit var logAdapter: BookingStatusLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bookingId = intent.getStringExtra("bookingId").orEmpty()
        if (bookingId.isEmpty()) {
            Toast.makeText(this, "Invalid booking", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        logAdapter = BookingStatusLogAdapter(emptyList())
        binding.rvStatusTimeline.layoutManager = LinearLayoutManager(this)
        binding.rvStatusTimeline.adapter = logAdapter

        loadBookingDetails(bookingId)
        loadBookingLogs(bookingId)
    }

    private fun loadBookingDetails(bookingId: String) {
        customerRepository.getBookingById(
            bookingId = bookingId,
            onSuccess = { booking ->
                binding.tvBookingType.text = "Type: ${booking.bookingType}"
                binding.tvStatus.text = "Status: ${booking.status}"
                binding.tvPickup.text = "Pickup: ${booking.pickupAddress}"
                binding.tvDrop.text = "Drop: ${booking.dropAddress}"
                binding.tvReceiver.text = "Receiver: ${booking.receiverName} (${booking.receiverPhone})"
                binding.tvPackage.text = "Package: ${booking.packageType} / ${booking.packageWeight}"
                binding.tvFare.text = "Estimated Fare: ৳${booking.estimatedFare.toInt()}"
                binding.tvDistance.text = "Distance: ${"%.2f".format(booking.distanceKm)} km"

                val formattedDate = if (booking.createdAt > 0L) {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(booking.createdAt))
                } else {
                    "Unknown time"
                }

                binding.tvCreatedAt.text = "Created At: $formattedDate"
            },
            onFailure = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadBookingLogs(bookingId: String) {
        customerRepository.getBookingStatusLogs(
            bookingId = bookingId,
            onSuccess = { logs ->
                logAdapter.updateData(logs)
            },
            onFailure = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }
}