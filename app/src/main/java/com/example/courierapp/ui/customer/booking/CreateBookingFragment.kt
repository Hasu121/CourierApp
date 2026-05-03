package com.example.courierapp.ui.customer.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.courierapp.R
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.BuildConfig
import com.example.courierapp.data.repository.BookingRepository
import com.example.courierapp.databinding.FragmentCreateBookingBinding
import com.example.courierapp.ui.common.LocationPickerDialogFragment
import com.example.courierapp.utils.Constants
import com.example.courierapp.utils.FareConfig
import com.example.courierapp.utils.LocationHelper
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CreateBookingFragment : Fragment() {

    private var _binding: FragmentCreateBookingBinding? = null
    private val binding get() = _binding!!

    private val bookingRepository = BookingRepository()
    private lateinit var locationHelper: LocationHelper

    private var pickupLat: Double = 0.0
    private var pickupLng: Double = 0.0
    private var dropLat: Double = 0.0
    private var dropLng: Double = 0.0

    private var pickupMarker: Marker? = null
    private var dropMarker: Marker? = null

    private val bookingTypeOptions = listOf(
        Constants.BOOKING_WITHIN_CITY,
        Constants.BOOKING_INTERCITY
    )

    private val vehicleOptions = listOf(
        Constants.VEHICLE_BIKE,
        Constants.VEHICLE_CAR,
        Constants.VEHICLE_TRUCK
    )

    private val packageWeightOptions = listOf(
        Constants.WEIGHT_LIGHT,
        Constants.WEIGHT_MEDIUM,
        Constants.WEIGHT_HEAVY
    )

    private val preferredTimeOptions = listOf(
        "Now",
        "Today Morning",
        "Today Evening",
        "Tomorrow"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = LocationHelper(requireActivity())

        binding.mapViewBooking.onCreate(savedInstanceState)

        setupMap()
        setupSpinners()
        setupFareRefreshListeners()
        updateCoordinateLabels()
        updateFareEstimate()

        binding.btnUseCurrentLocation.setOnClickListener {
            useCurrentLocation()
        }

        binding.btnCenterPickup.setOnClickListener {
            if (pickupLat == 0.0 && pickupLng == 0.0) {
                Toast.makeText(requireContext(), "Pickup not set", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.mapViewBooking.getMapAsync { map ->
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(pickupLat, pickupLng),
                        15.0
                    )
                )
            }
        }

        binding.btnCenterDrop.setOnClickListener {
            if (dropLat == 0.0 && dropLng == 0.0) {
                Toast.makeText(requireContext(), "Drop not set", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.mapViewBooking.getMapAsync { map ->
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(dropLat, dropLng),
                        15.0
                    )
                )
            }
        }

        binding.btnCreateBooking.setOnClickListener {
            createBooking()
        }

        binding.btnPickPickupOnMap.setOnClickListener {
            LocationPickerDialogFragment(
                titleText = "Pick Pickup Location",
                initialLat = pickupLat,
                initialLng = pickupLng
            ) { lat, lng ->
                pickupLat = lat
                pickupLng = lng
                binding.etPickupAddress.setText("Picked on map ($lat, $lng)")
                updateCoordinateLabels()
                updateFareEstimate()
                refreshPreviewMap()
            }.show(parentFragmentManager, "pickup_picker")
        }

        binding.btnPickDropOnMap.setOnClickListener {
            LocationPickerDialogFragment(
                titleText = "Pick Drop Location",
                initialLat = dropLat,
                initialLng = dropLng
            ) { lat, lng ->
                dropLat = lat
                dropLng = lng
                binding.etDropAddress.setText("Picked on map ($lat, $lng)")
                updateCoordinateLabels()
                updateFareEstimate()
                refreshPreviewMap()
            }.show(parentFragmentManager, "drop_picker")
        }
    }

    private fun makeSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_selected,
            items
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }

    private fun setupMap() {
        binding.mapViewBooking.getMapAsync { map ->
            val styleUrl =
                "https://tiles.stadiamaps.com/styles/outdoors.json?api_key=${BuildConfig.STADIA_API_KEY}"

            android.util.Log.d("MAP_DEBUG", "Booking key length = ${BuildConfig.STADIA_API_KEY.length}")
            android.util.Log.d("MAP_DEBUG", "Booking style URL = $styleUrl")

            map.setStyle(styleUrl) {
                android.util.Log.d("MAP_DEBUG", "Booking style loaded successfully")

                val dhaka = LatLng(23.8103, 90.4125)

                map.cameraPosition = CameraPosition.Builder()
                    .target(dhaka)
                    .zoom(12.5)
                    .build()

                map.addOnMapClickListener { latLng ->
                    dropLat = latLng.latitude
                    dropLng = latLng.longitude

                    dropMarker?.let { map.removeMarker(it) }
                    dropMarker = map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Drop Location")
                    )

                    binding.etDropAddress.setText("Selected Drop ($dropLat, $dropLng)")
                    updateCoordinateLabels()
                    updateFareEstimate()

                    Toast.makeText(requireContext(), "Drop location selected", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
    }

    private fun setupSpinners() {
        binding.spinnerBookingType.adapter = makeSpinnerAdapter(bookingTypeOptions)
        binding.spinnerVehicleType.adapter = makeSpinnerAdapter(vehicleOptions)
        binding.spinnerPackageWeight.adapter = makeSpinnerAdapter(packageWeightOptions)
        binding.spinnerPreferredTime.adapter = makeSpinnerAdapter(preferredTimeOptions)
    }

    private fun setupFareRefreshListeners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateFareEstimate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerBookingType.onItemSelectedListener = listener
        binding.spinnerVehicleType.onItemSelectedListener = listener
        binding.spinnerPackageWeight.onItemSelectedListener = listener
    }

    private fun useCurrentLocation() {
        if (!locationHelper.hasLocationPermission()) {
            locationHelper.requestLocationPermission()
            Toast.makeText(
                requireContext(),
                "Please allow location permission and tap again",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        locationHelper.getCurrentLocation(
            onSuccess = { lat, lng ->
                pickupLat = lat
                pickupLng = lng

                binding.etPickupAddress.setText("Current Location ($lat, $lng)")
                updateCoordinateLabels()
                updateFareEstimate()

                binding.mapViewBooking.getMapAsync { map ->
                    val newLocation = LatLng(lat, lng)

                    pickupMarker?.let { map.removeMarker(it) }
                    pickupMarker = map.addMarker(
                        MarkerOptions()
                            .position(newLocation)
                            .title("Pickup Location")
                    )

                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(newLocation, 15.0)
                    )
                }

                Toast.makeText(requireContext(), "Pickup location updated", Toast.LENGTH_SHORT).show()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateFareEstimate() {
        val bookingType = binding.spinnerBookingType.selectedItem?.toString()
            ?: Constants.BOOKING_WITHIN_CITY

        val vehicleType = binding.spinnerVehicleType.selectedItem?.toString()
            ?: Constants.VEHICLE_BIKE

        val packageWeight = binding.spinnerPackageWeight.selectedItem?.toString()
            ?: Constants.WEIGHT_LIGHT

        if ((pickupLat == 0.0 && pickupLng == 0.0) || (dropLat == 0.0 && dropLng == 0.0)) {
            binding.tvEstimatedFare.text = "Estimated Fare: ৳0"
            return
        }

        val distanceKm = calculateDistanceKm(
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropLat = dropLat,
            dropLng = dropLng
        )

        val estimatedFare = calculateFare(
            bookingType = bookingType,
            vehicleType = vehicleType,
            packageWeight = packageWeight,
            distanceKm = distanceKm
        )

        binding.tvEstimatedFare.text =
            "Estimated Fare: ৳${estimatedFare.toInt()} | Distance: ${"%.2f".format(distanceKm)} km"
    }

    private fun calculateFare(
        bookingType: String,
        vehicleType: String,
        packageWeight: String,
        distanceKm: Double
    ): Double {
        val baseFare = when (bookingType) {
            Constants.BOOKING_WITHIN_CITY -> FareConfig.WITHIN_CITY_BASE_FARE
            Constants.BOOKING_INTERCITY -> FareConfig.INTERCITY_BASE_FARE
            else -> FareConfig.WITHIN_CITY_BASE_FARE
        }

        val perKmRate = when (vehicleType) {
            Constants.VEHICLE_BIKE -> FareConfig.PER_KM_BIKE
            Constants.VEHICLE_CAR -> FareConfig.PER_KM_CAR
            Constants.VEHICLE_TRUCK -> FareConfig.PER_KM_TRUCK
            else -> FareConfig.PER_KM_BIKE
        }

        val weightCharge = when (packageWeight) {
            Constants.WEIGHT_LIGHT -> FareConfig.WEIGHT_LIGHT
            Constants.WEIGHT_MEDIUM -> FareConfig.WEIGHT_MEDIUM
            Constants.WEIGHT_HEAVY -> FareConfig.WEIGHT_HEAVY
            else -> FareConfig.WEIGHT_LIGHT
        }

        return baseFare + (distanceKm * perKmRate) + weightCharge
    }

    private fun calculateDistanceKm(
        pickupLat: Double,
        pickupLng: Double,
        dropLat: Double,
        dropLng: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(dropLat - pickupLat)
        val dLng = Math.toRadians(dropLng - pickupLng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(pickupLat)) *
                cos(Math.toRadians(dropLat)) *
                sin(dLng / 2) *
                sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    private fun updateCoordinateLabels() {
        binding.tvPickupCoords.text =
            if (pickupLat != 0.0 || pickupLng != 0.0) {
                "Pickup Coords: $pickupLat, $pickupLng"
            } else {
                "Pickup Coords: Not set"
            }

        binding.tvDropCoords.text =
            if (dropLat != 0.0 || dropLng != 0.0) {
                "Drop Coords: $dropLat, $dropLng"
            } else {
                "Drop Coords: Not set"
            }
    }

    private fun refreshPreviewMap() {
        binding.mapViewBooking.getMapAsync { map ->
            if (pickupLat != 0.0 || pickupLng != 0.0) {
                pickupMarker?.let { map.removeMarker(it) }
                pickupMarker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(pickupLat, pickupLng))
                        .title("Pickup")
                )
            }

            if (dropLat != 0.0 || dropLng != 0.0) {
                dropMarker?.let { map.removeMarker(it) }
                dropMarker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(dropLat, dropLng))
                        .title("Drop")
                )
            }

            when {
                dropLat != 0.0 || dropLng != 0.0 -> {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(dropLat, dropLng),
                            14.0
                        )
                    )
                }

                pickupLat != 0.0 || pickupLng != 0.0 -> {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(pickupLat, pickupLng),
                            14.0
                        )
                    )
                }
            }
        }
    }

    private fun createBooking() {
        val bookingType = binding.spinnerBookingType.selectedItem.toString()
        val vehicleType = binding.spinnerVehicleType.selectedItem.toString()
        val pickupAddress = binding.etPickupAddress.text.toString().trim()
        val dropAddress = binding.etDropAddress.text.toString().trim()
        val packageType = binding.etPackageType.text.toString().trim()
        val packageWeight = binding.spinnerPackageWeight.selectedItem.toString()
        val packageNote = binding.etPackageNote.text.toString().trim()
        val receiverName = binding.etReceiverName.text.toString().trim()
        val receiverPhone = binding.etReceiverPhone.text.toString().trim()
        val preferredTime = binding.spinnerPreferredTime.selectedItem.toString()

        if (
            pickupAddress.isEmpty() ||
            dropAddress.isEmpty() ||
            packageType.isEmpty() ||
            receiverName.isEmpty() ||
            receiverPhone.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (pickupLat == 0.0 && pickupLng == 0.0) {
            Toast.makeText(requireContext(), "Please set pickup location", Toast.LENGTH_SHORT).show()
            return
        }

        if (dropLat == 0.0 && dropLng == 0.0) {
            Toast.makeText(requireContext(), "Please tap map to set drop location", Toast.LENGTH_SHORT).show()
            return
        }

        val distanceKm = calculateDistanceKm(
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropLat = dropLat,
            dropLng = dropLng
        )

        val estimatedFare = calculateFare(
            bookingType = bookingType,
            vehicleType = vehicleType,
            packageWeight = packageWeight,
            distanceKm = distanceKm
        )

        bookingRepository.createBooking(
            bookingType = bookingType,
            vehicleType = vehicleType,
            pickupAddress = pickupAddress,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropAddress = dropAddress,
            dropLat = dropLat,
            dropLng = dropLng,
            packageType = packageType,
            packageWeight = packageWeight,
            packageNote = packageNote,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            preferredTime = preferredTime,
            distanceKm = distanceKm,
            estimatedFare = estimatedFare,
            onSuccess = {
                Toast.makeText(requireContext(), "Booking created successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun clearForm() {
        binding.etPickupAddress.text?.clear()
        binding.etDropAddress.text?.clear()
        binding.etPackageType.text?.clear()
        binding.etPackageNote.text?.clear()
        binding.etReceiverName.text?.clear()
        binding.etReceiverPhone.text?.clear()

        binding.spinnerBookingType.setSelection(0)
        binding.spinnerVehicleType.setSelection(0)
        binding.spinnerPackageWeight.setSelection(0)
        binding.spinnerPreferredTime.setSelection(0)

        pickupLat = 0.0
        pickupLng = 0.0
        dropLat = 0.0
        dropLng = 0.0

        updateCoordinateLabels()
        updateFareEstimate()

        binding.mapViewBooking.getMapAsync { map ->
            pickupMarker?.let { map.removeMarker(it) }
            dropMarker?.let { map.removeMarker(it) }
            pickupMarker = null
            dropMarker = null
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapViewBooking.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewBooking.onResume()
    }

    override fun onPause() {
        binding.mapViewBooking.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapViewBooking.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewBooking.onLowMemory()
    }

    override fun onDestroyView() {
        binding.mapViewBooking.onDestroy()
        _binding = null
        super.onDestroyView()
    }
}