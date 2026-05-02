package com.example.courierapp.ui.customer.history

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courierapp.BuildConfig
import com.example.courierapp.R
import com.example.courierapp.data.model.Booking
import com.example.courierapp.data.repository.CustomerRepository
import com.example.courierapp.databinding.ActivityBookingDetailsBinding
import com.example.courierapp.ui.common.BookingStatusLogAdapter
import com.example.courierapp.utils.Constants
import org.maplibre.android.MapLibre
import com.example.courierapp.ui.common.TrackingMapDialogFragment
import com.example.courierapp.data.repository.ReviewRepository
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class BookingDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailsBinding
    private val customerRepository = CustomerRepository()
    private lateinit var logAdapter: BookingStatusLogAdapter

    private val reviewRepository = ReviewRepository()

    private var currentBooking: Booking? = null

    private var pickupMarker: Marker? = null
    private var dropMarker: Marker? = null
    private var driverMarker: Marker? = null
    private var driverLat: Double = 0.0
    private var driverLng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        binding = ActivityBookingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.courier_green)
        window.navigationBarColor = getColor(R.color.courier_light_bg)

        binding.mapViewTracking.onCreate(savedInstanceState)

        val bookingId = intent.getStringExtra("bookingId").orEmpty()
        if (bookingId.isEmpty()) {
            Toast.makeText(this, "Invalid booking", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSubmitCustomerReview.setOnClickListener {
            submitCustomerReview()
        }

        binding.btnExpandTrackingMap.setOnClickListener {
            val booking = currentBooking

            if (booking == null) {
                Toast.makeText(this, "Booking not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            TrackingMapDialogFragment(
                booking = booking,
                driverLat = driverLat,
                driverLng = driverLng
            ).show(supportFragmentManager, "tracking_map")
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRefreshTracking.setOnClickListener {
            currentBooking?.let {
                setupTrackingMap(it)
            }
        }

        binding.btnCancelBooking.setOnClickListener {
            val booking = currentBooking

            if (booking == null) {
                Toast.makeText(this, "Booking not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            customerRepository.cancelBooking(
                booking = booking,
                onSuccess = {
                    Toast.makeText(this, "Booking cancelled. Penalty will apply to your next booking.", Toast.LENGTH_LONG).show()
                    loadBookingDetails(booking.bookingId)
                    loadBookingLogs(booking.bookingId)
                },
                onFailure = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }

        logAdapter = BookingStatusLogAdapter(emptyList())
        binding.rvStatusTimeline.layoutManager = LinearLayoutManager(this)
        binding.rvStatusTimeline.adapter = logAdapter

        setupEmptyMap()
        loadBookingDetails(bookingId)
        loadBookingLogs(bookingId)
    }

    private fun setupEmptyMap() {
        binding.mapViewTracking.getMapAsync { map ->
            val styleUrl =
                "https://tiles.stadiamaps.com/styles/outdoors.json?api_key=${BuildConfig.STADIA_API_KEY}"

            map.setStyle(Style.Builder().fromUri(styleUrl)) {
                val dhaka = LatLng(23.8103, 90.4125)
                map.cameraPosition = CameraPosition.Builder()
                    .target(dhaka)
                    .zoom(11.0)
                    .build()
            }
        }
    }

    private fun loadBookingDetails(bookingId: String) {
        customerRepository.getBookingById(
            bookingId = bookingId,
            onSuccess = { booking ->
                currentBooking = booking

                binding.tvBookingType.text = "Type: ${booking.bookingType}"
                binding.tvStatus.text = "Status: ${booking.status}"
                binding.tvPickup.text = "Pickup: ${booking.pickupAddress}"
                binding.tvDrop.text = "Drop: ${booking.dropAddress}"
                binding.tvReceiver.text = "Receiver: ${booking.receiverName} (${booking.receiverPhone})"
                binding.tvPackage.text = "Package: ${booking.packageType} / ${booking.packageWeight}"
                binding.tvFare.text = "Estimated Fare: ৳${booking.estimatedFare.toInt()}"
                binding.tvDistance.text = "Distance: ${"%.2f".format(booking.distanceKm)} km"

                binding.tvAssignedDriver.text = if (booking.assignedDriverId.isEmpty()) {
                    "Assigned Driver: Not assigned"
                } else {
                    "Assigned Driver: ${booking.assignedDriverId}"
                }

                binding.btnCancelBooking.visibility = if (
                    booking.status == Constants.STATUS_PENDING ||
                    booking.status == Constants.STATUS_ACCEPTED
                ) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                val formattedDate = if (booking.createdAt > 0L) {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(booking.createdAt))
                } else {
                    "Unknown time"
                }

                binding.tvCreatedAt.text = "Created At: $formattedDate"

                showCustomerReviewSectionIfAllowed(booking)

                setupTrackingMap(booking)
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

    private fun submitCustomerReview() {
        val booking = currentBooking
        if (booking == null) {
            Toast.makeText(this, "Booking not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val ratingText = binding.etCustomerRating.text.toString().trim()
        val comment = binding.etCustomerReviewComment.text.toString().trim()

        val rating = ratingText.toIntOrNull()
        if (rating == null || rating < 1 || rating > 5) {
            Toast.makeText(this, "Enter rating from 1 to 5", Toast.LENGTH_SHORT).show()
            return
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            return
        }

        reviewRepository.submitCustomerReviewForDriver(
            booking = booking,
            rating = rating,
            comment = comment,
            onSuccess = {
                Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show()
                binding.etCustomerRating.text?.clear()
                binding.etCustomerReviewComment.text?.clear()
                hideCustomerReviewSection()
            },
            onFailure = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun hideCustomerReviewSection() {
        binding.tvCustomerReviewTitle.visibility = android.view.View.GONE
        binding.etCustomerRating.visibility = android.view.View.GONE
        binding.etCustomerReviewComment.visibility = android.view.View.GONE
        binding.btnSubmitCustomerReview.visibility = android.view.View.GONE
    }

    private fun showCustomerReviewSectionIfAllowed(booking: Booking) {
        if (booking.status != Constants.STATUS_DELIVERED) {
            hideCustomerReviewSection()
            return
        }

        reviewRepository.checkCustomerReviewed(
            bookingId = booking.bookingId,
            onResult = { alreadyReviewed ->
                if (alreadyReviewed) {
                    hideCustomerReviewSection()
                } else {
                    binding.tvCustomerReviewTitle.visibility = android.view.View.VISIBLE
                    binding.etCustomerRating.visibility = android.view.View.VISIBLE
                    binding.etCustomerReviewComment.visibility = android.view.View.VISIBLE
                    binding.btnSubmitCustomerReview.visibility = android.view.View.VISIBLE
                }
            }
        )
    }

    private fun setupTrackingMap(booking: Booking) {
        binding.mapViewTracking.getMapAsync { map ->
            val styleUrl =
                "https://tiles.stadiamaps.com/styles/outdoors.json?api_key=${BuildConfig.STADIA_API_KEY}"

            map.setStyle(Style.Builder().fromUri(styleUrl)) {
                pickupMarker?.let { map.removeMarker(it) }
                dropMarker?.let { map.removeMarker(it) }
                driverMarker?.let { map.removeMarker(it) }

                val pickup = LatLng(booking.pickupLat, booking.pickupLng)
                val drop = LatLng(booking.dropLat, booking.dropLng)

                if (booking.pickupLat != 0.0 || booking.pickupLng != 0.0) {
                    pickupMarker = map.addMarker(
                        MarkerOptions()
                            .position(pickup)
                            .title("Pickup")
                    )
                }

                if (booking.dropLat != 0.0 || booking.dropLng != 0.0) {
                    dropMarker = map.addMarker(
                        MarkerOptions()
                            .position(drop)
                            .title("Drop")
                    )
                }

                val cameraTarget = when {
                    booking.dropLat != 0.0 || booking.dropLng != 0.0 -> drop
                    booking.pickupLat != 0.0 || booking.pickupLng != 0.0 -> pickup
                    else -> LatLng(23.8103, 90.4125)
                }

                map.cameraPosition = CameraPosition.Builder()
                    .target(cameraTarget)
                    .zoom(13.5)
                    .build()

                if (booking.assignedDriverId.isNotEmpty()) {
                    loadDriverMarker(booking.assignedDriverId)
                } else {
                    binding.tvMapInfo.text = "Map preview: pickup and drop shown. Driver not assigned yet."
                }
            }
        }
    }

    private fun loadDriverMarker(driverId: String) {
        customerRepository.getDriverLocation(
            driverId = driverId,
            onSuccess = { lat, lng ->
                driverLat = lat
                driverLng = lng

                binding.mapViewTracking.getMapAsync { map ->
                    val driverLatLng = LatLng(lat, lng)

                    driverMarker?.let { map.removeMarker(it) }
                    driverMarker = map.addMarker(
                        MarkerOptions()
                            .position(driverLatLng)
                            .title("Driver")
                    )

                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(driverLatLng, 14.0)
                    )

                    binding.tvMapInfo.text = "Map preview: pickup, drop, and driver location shown."
                }
            },
            onFailure = { message ->
                driverLat = 0.0
                driverLng = 0.0
                binding.tvMapInfo.text = "Map preview: pickup and drop shown. $message"
            }
        )
    }

    override fun onStart() {
        super.onStart()
        binding.mapViewTracking.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewTracking.onResume()
    }

    override fun onPause() {
        binding.mapViewTracking.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapViewTracking.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewTracking.onLowMemory()
    }

    override fun onDestroy() {
        binding.mapViewTracking.onDestroy()
        super.onDestroy()
    }
}