package com.example.courierapp.ui.driver.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.databinding.FragmentDriverHomeBinding

class DriverHomeFragment : Fragment() {

    private var _binding: FragmentDriverHomeBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()

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

        loadDriverData()
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