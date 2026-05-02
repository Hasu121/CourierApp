package com.example.courierapp.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.courierapp.BuildConfig
import com.example.courierapp.data.model.Booking
import com.example.courierapp.databinding.DialogTrackingMapBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import com.example.courierapp.utils.InsetHelper

class TrackingMapDialogFragment(
    private val booking: Booking,
    private val driverLat: Double,
    private val driverLng: Double
) : DialogFragment() {

    private var _binding: DialogTrackingMapBinding? = null
    private val binding get() = _binding!!

    private var mapLibreMap: MapLibreMap? = null

    private var pickupMarker: Marker? = null
    private var dropMarker: Marker? = null
    private var driverMarker: Marker? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        _binding = DialogTrackingMapBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(binding.root)

        InsetHelper.applySystemBarPadding(
            view = binding.trackingTopControls,
            applyTop = true,
            applyBottom = false
        )

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        MapLibre.getInstance(requireContext())
        binding.mapViewFullTracking.onCreate(savedInstanceState)

        setupMap()

        binding.btnCloseTrackingMap.setOnClickListener {
            dismiss()
        }

        binding.btnCenterPickupFull.setOnClickListener {
            centerMap(booking.pickupLat, booking.pickupLng, "Pickup not available")
        }

        binding.btnCenterDropFull.setOnClickListener {
            centerMap(booking.dropLat, booking.dropLng, "Drop not available")
        }

        binding.btnCenterDriverFull.setOnClickListener {
            centerMap(driverLat, driverLng, "Driver location not available")
        }

        return dialog
    }

    private fun setupMap() {
        binding.mapViewFullTracking.getMapAsync { map ->
            mapLibreMap = map

            val styleUrl =
                "https://tiles.stadiamaps.com/styles/outdoors.json?api_key=${BuildConfig.STADIA_API_KEY}"

            map.setStyle(Style.Builder().fromUri(styleUrl)) {
                addMarkers(map)

                val target = when {
                    driverLat != 0.0 || driverLng != 0.0 -> LatLng(driverLat, driverLng)
                    booking.dropLat != 0.0 || booking.dropLng != 0.0 -> LatLng(booking.dropLat, booking.dropLng)
                    booking.pickupLat != 0.0 || booking.pickupLng != 0.0 -> LatLng(booking.pickupLat, booking.pickupLng)
                    else -> LatLng(23.8103, 90.4125)
                }

                map.cameraPosition = CameraPosition.Builder()
                    .target(target)
                    .zoom(14.0)
                    .build()
            }
        }
    }

    private fun addMarkers(map: MapLibreMap) {
        if (booking.pickupLat != 0.0 || booking.pickupLng != 0.0) {
            pickupMarker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(booking.pickupLat, booking.pickupLng))
                    .title("Pickup")
            )
        }

        if (booking.dropLat != 0.0 || booking.dropLng != 0.0) {
            dropMarker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(booking.dropLat, booking.dropLng))
                    .title("Drop")
            )
        }

        if (driverLat != 0.0 || driverLng != 0.0) {
            driverMarker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(driverLat, driverLng))
                    .title("Driver")
            )
        }
    }

    private fun centerMap(lat: Double, lng: Double, errorMessage: String) {
        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        mapLibreMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(lat, lng),
                15.0
            )
        )
    }

    override fun onStart() {
        super.onStart()
        binding.mapViewFullTracking.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewFullTracking.onResume()
    }

    override fun onPause() {
        binding.mapViewFullTracking.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapViewFullTracking.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewFullTracking.onLowMemory()
    }

    override fun onDestroyView() {
        binding.mapViewFullTracking.onDestroy()
        _binding = null
        super.onDestroyView()
    }
}