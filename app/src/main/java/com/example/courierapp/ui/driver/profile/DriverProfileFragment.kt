package com.example.courierapp.ui.driver.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.data.repository.ProfileRepository
import com.example.courierapp.databinding.FragmentDriverProfileBinding

class DriverProfileFragment : Fragment() {

    private var _binding: FragmentDriverProfileBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()
    private val profileRepository = ProfileRepository()

    private var isEditing = false

    private var originalFullName = ""
    private var originalEmail = ""
    private var originalPhone = ""
    private var originalAddress = ""
    private var originalVehicleType = ""
    private var originalVehicleNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEditingEnabled(false)
        loadDriverProfile()

        binding.btnChangeInfo.setOnClickListener {
            if (isEditing) {
                restoreOriginalValues()
                setEditingEnabled(false)
            } else {
                saveOriginalValues()
                setEditingEnabled(true)
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            if (!isEditing) {
                Toast.makeText(requireContext(), "Tap Change Information first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveDriverProfile()
        }
    }

    private fun loadDriverProfile() {
        driverRepository.getCurrentDriverUser(
            onSuccess = { user ->
                binding.etFullName.setText(user.fullName)
                binding.etEmail.setText(user.email)
                binding.etPhone.setText(user.phone)
                binding.etAddress.setText(user.address)

                saveOriginalValues()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )

        driverRepository.getCurrentDriverProfile(
            onSuccess = { profile ->
                binding.etVehicleType.setText(profile.vehicleType)
                binding.etVehicleNumber.setText(profile.vehicleNumber)
                binding.tvRating.text = "Rating: ${"%.1f".format(profile.rating)}"

                saveOriginalValues()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveDriverProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val vehicleType = binding.etVehicleType.text.toString().trim()
        val vehicleNumber = binding.etVehicleNumber.text.toString().trim()

        if (
            fullName.isEmpty() ||
            phone.isEmpty() ||
            address.isEmpty() ||
            vehicleType.isEmpty() ||
            vehicleNumber.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        profileRepository.updateDriverProfile(
            fullName = fullName,
            phone = phone,
            address = address,
            vehicleType = vehicleType,
            vehicleNumber = vehicleNumber,
            onSuccess = {
                Toast.makeText(requireContext(), "Driver profile updated", Toast.LENGTH_SHORT).show()
                saveOriginalValues()
                setEditingEnabled(false)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setEditingEnabled(enabled: Boolean) {
        isEditing = enabled

        binding.etFullName.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        binding.etAddress.isEnabled = enabled
        binding.etVehicleType.isEnabled = enabled
        binding.etVehicleNumber.isEnabled = enabled

        binding.etEmail.isEnabled = false

        binding.btnChangeInfo.text = if (enabled) {
            "Cancel Changes"
        } else {
            "Change Information"
        }

        binding.btnSaveProfile.alpha = if (enabled) 1.0f else 0.55f
    }

    private fun saveOriginalValues() {
        originalFullName = binding.etFullName.text.toString()
        originalEmail = binding.etEmail.text.toString()
        originalPhone = binding.etPhone.text.toString()
        originalAddress = binding.etAddress.text.toString()
        originalVehicleType = binding.etVehicleType.text.toString()
        originalVehicleNumber = binding.etVehicleNumber.text.toString()
    }

    private fun restoreOriginalValues() {
        binding.etFullName.setText(originalFullName)
        binding.etEmail.setText(originalEmail)
        binding.etPhone.setText(originalPhone)
        binding.etAddress.setText(originalAddress)
        binding.etVehicleType.setText(originalVehicleType)
        binding.etVehicleNumber.setText(originalVehicleNumber)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}