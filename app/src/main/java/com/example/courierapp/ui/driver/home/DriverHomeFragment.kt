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
                binding.tvWelcomeDriver.text = "Welcome, ${user.fullName}"
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )

        driverRepository.getCurrentDriverProfile(
            onSuccess = { profile ->
                binding.tvVehicleType.text = "Vehicle Type: ${profile.vehicleType}"
                binding.tvVehicleNumber.text = "Vehicle Number: ${profile.vehicleNumber}"
                binding.tvLicenseNumber.text = "License Number: ${profile.licenseNumber}"
                binding.tvVerificationStatus.text = "Verification Status: ${profile.verificationStatus}"
                binding.tvServiceMode.text = "Service Mode: ${profile.serviceMode.joinToString()}"
                binding.tvAvailability.text = "Availability: ${if (profile.isAvailable) "Online" else "Offline"}"
                binding.switchAvailability.isChecked = profile.isAvailable
            },
            onFailure = { message ->
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
                        binding.tvCurrentLocation.text = "Current Location: $lat, $lng"
                        Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}