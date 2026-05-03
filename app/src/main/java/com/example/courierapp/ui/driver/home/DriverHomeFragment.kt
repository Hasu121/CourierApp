package com.example.courierapp.ui.driver.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.databinding.FragmentDriverHomeBinding
import com.example.courierapp.utils.LocationHelper

class DriverHomeFragment : Fragment() {

    private var _binding: FragmentDriverHomeBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()
    private lateinit var locationHelper: LocationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = LocationHelper(requireActivity())
        loadDriverData()
        setupListeners()
    }

    private fun loadDriverData() {
        driverRepository.getCurrentDriverUser(
            onSuccess = { user ->
                val safeBinding = _binding ?: return@getCurrentDriverUser
                safeBinding.tvWelcomeDriver.text = "Welcome, ${user.fullName}"
            },
            onFailure = { message ->
                if (!isAdded) return@getCurrentDriverUser
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )

        driverRepository.getCurrentDriverProfile(
            onSuccess = { profile ->
                val safeBinding = _binding ?: return@getCurrentDriverProfile

                val vehiclesText = if (profile.vehicleTypes.isEmpty()) {
                    "Not set"
                } else {
                    profile.vehicleTypes.joinToString(", ")
                }

                val serviceModeText = if (profile.serviceMode.isEmpty()) {
                    "Not set"
                } else {
                    profile.serviceMode.joinToString(", ")
                }

                safeBinding.tvVehicleType.text = "Vehicle Types: $vehiclesText"
                safeBinding.tvVehicleNumber.text = "Vehicle Number: ${profile.vehicleNumber}"
                safeBinding.tvLicenseNumber.text = "License Number: ${profile.licenseNumber}"
                safeBinding.tvVerificationStatus.text = "Verification Status: ${profile.verificationStatus}"
                safeBinding.tvServiceMode.text = "Service Mode: $serviceModeText"

                safeBinding.tvAvailability.text =
                    "Availability: ${if (profile.isAvailable) "Online" else "Offline"}"

                safeBinding.switchAvailability.setOnCheckedChangeListener(null)
                safeBinding.switchAvailability.isChecked = profile.isAvailable
                safeBinding.switchAvailability.text =
                    if (profile.isAvailable) "Go Offline" else "Go Online"

                safeBinding.switchAvailability.setOnCheckedChangeListener { _, isChecked ->
                    driverRepository.updateAvailability(
                        isAvailable = isChecked,
                        onSuccess = {
                            val currentBinding = _binding ?: return@updateAvailability

                            currentBinding.tvAvailability.text =
                                "Availability: ${if (isChecked) "Online" else "Offline"}"

                            currentBinding.switchAvailability.text =
                                if (isChecked) "Go Offline" else "Go Online"

                            if (isAdded) {
                                Toast.makeText(requireContext(), "Availability updated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFailure = { message ->
                            val currentBinding = _binding ?: return@updateAvailability

                            currentBinding.switchAvailability.setOnCheckedChangeListener(null)
                            currentBinding.switchAvailability.isChecked = !isChecked
                            currentBinding.switchAvailability.text =
                                if (!isChecked) "Go Offline" else "Go Online"

                            currentBinding.switchAvailability.setOnCheckedChangeListener { _, checked ->
                                driverRepository.updateAvailability(
                                    isAvailable = checked,
                                    onSuccess = {
                                        val b = _binding ?: return@updateAvailability
                                        b.tvAvailability.text =
                                            "Availability: ${if (checked) "Online" else "Offline"}"
                                        b.switchAvailability.text =
                                            if (checked) "Go Offline" else "Go Online"
                                    },
                                    onFailure = {}
                                )
                            }

                            if (isAdded) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            },
            onFailure = { message ->
                if (!isAdded) return@getCurrentDriverProfile
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupListeners() {
        binding.switchAvailability.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            driverRepository.updateAvailability(
                isAvailable = isChecked,
                onSuccess = {
                    binding.tvAvailability.text = "Availability: ${if (isChecked) "Online" else "Offline"}"
                    Toast.makeText(requireContext(), "Availability updated", Toast.LENGTH_SHORT).show()
                },
                onFailure = { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnUpdateLocation.setOnClickListener {
            updateCurrentLocation()
        }
    }

    private fun updateCurrentLocation() {
        if (!locationHelper.hasLocationPermission()) {
            locationHelper.requestLocationPermission()
            Toast.makeText(requireContext(), "Allow location permission and tap again", Toast.LENGTH_SHORT).show()
            return
        }

        locationHelper.getCurrentLocation(
            onSuccess = { lat, lng ->
                driverRepository.updateDriverLocation(
                    lat = lat,
                    lng = lng,
                    onSuccess = {
                        val safeBinding = _binding ?: return@updateDriverLocation
                        safeBinding.tvCurrentLocation.text = "Current Location: $lat, $lng"

                        if (isAdded) {
                            Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { message ->
                        if (isAdded) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            onFailure = { message ->
                if (isAdded) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}