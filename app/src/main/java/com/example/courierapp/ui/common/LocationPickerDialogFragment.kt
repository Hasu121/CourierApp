package com.example.courierapp.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.courierapp.BuildConfig
import com.example.courierapp.databinding.DialogLocationPickerBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

class LocationPickerDialogFragment(
    private val titleText: String,
    private val initialLat: Double,
    private val initialLng: Double,
    private val onConfirm: (Double, Double) -> Unit
) : DialogFragment() {

    private var _binding: DialogLocationPickerBinding? = null
    private val binding get() = _binding!!

    private var mapLibreMap: MapLibreMap? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        _binding = DialogLocationPickerBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(binding.root)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        binding.tvPickerTitle.text = titleText

        MapLibre.getInstance(requireContext())
        binding.mapViewPicker.onCreate(savedInstanceState)

        binding.mapViewPicker.getMapAsync { map ->
            mapLibreMap = map

            val styleUrl =
                "https://tiles.stadiamaps.com/styles/outdoors.json?api_key=${BuildConfig.STADIA_API_KEY}"

            map.setStyle(Style.Builder().fromUri(styleUrl)) {
                val startLat = if (initialLat != 0.0) initialLat else 23.8103
                val startLng = if (initialLng != 0.0) initialLng else 90.4125

                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(startLat, startLng))
                    .zoom(15.0)
                    .build()
            }
        }

        binding.btnConfirmLocation.setOnClickListener {
            val center = mapLibreMap?.cameraPosition?.target
            if (center != null) {
                onConfirm(center.latitude, center.longitude)
                dismiss()
            }
        }

        binding.btnClosePicker.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        binding.mapViewPicker.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewPicker.onResume()
    }

    override fun onPause() {
        binding.mapViewPicker.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapViewPicker.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewPicker.onLowMemory()
    }

    override fun onDestroyView() {
        binding.mapViewPicker.onDestroy()
        _binding = null
        super.onDestroyView()
    }
}