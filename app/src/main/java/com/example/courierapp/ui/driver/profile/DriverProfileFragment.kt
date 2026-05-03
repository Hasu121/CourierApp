package com.example.courierapp.ui.driver.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.R
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.data.repository.ProfileRepository
import com.example.courierapp.databinding.FragmentDriverProfileBinding
import com.example.courierapp.utils.Constants

class DriverProfileFragment : Fragment() {

    private var _binding: FragmentDriverProfileBinding? = null
    private val binding get() = _binding!!

    private val driverRepository = DriverRepository()
    private val profileRepository = ProfileRepository()

    private var isEditing = false
    private var originalBikeChecked = false
    private var originalCarChecked = false
    private var originalTruckChecked = false
    private var originalFullName = ""
    private var originalEmail = ""
    private var originalPhone = ""
    private var originalAddress = ""
    private var originalVehicleNumber = ""
    private var originalServiceModeIndex = 0

    private val serviceOptions = listOf(
        Constants.SERVICE_WITHIN_CITY,
        Constants.SERVICE_INTERCITY,
        Constants.SERVICE_BOTH
    )

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

        setupSpinners()
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

    private fun setupSpinners() {
        binding.spServiceMode.adapter = makeSpinnerAdapter(serviceOptions)
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
                binding.cbBike.isChecked = profile.vehicleTypes.contains(Constants.VEHICLE_BIKE)
                binding.cbCar.isChecked = profile.vehicleTypes.contains(Constants.VEHICLE_CAR)
                binding.cbTruck.isChecked = profile.vehicleTypes.contains(Constants.VEHICLE_TRUCK)

                binding.etVehicleNumber.setText(profile.vehicleNumber)
                binding.tvRating.text = "Rating: ${"%.1f".format(profile.rating)}"

                val serviceValue = if (
                    profile.serviceMode.contains(Constants.SERVICE_WITHIN_CITY) &&
                    profile.serviceMode.contains(Constants.SERVICE_INTERCITY)
                ) {
                    Constants.SERVICE_BOTH
                } else {
                    profile.serviceMode.firstOrNull() ?: Constants.SERVICE_WITHIN_CITY
                }

                val serviceIndex = serviceOptions.indexOf(serviceValue)
                if (serviceIndex >= 0) {
                    binding.spServiceMode.setSelection(serviceIndex)
                }

                saveOriginalValues()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
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

    private fun saveDriverProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val vehicleNumber = binding.etVehicleNumber.text.toString().trim()
        val serviceModeValue = binding.spServiceMode.selectedItem.toString()
        val vehicleTypes = mutableListOf<String>()

        if (binding.cbBike.isChecked) {
            vehicleTypes.add(Constants.VEHICLE_BIKE)
        }

        if (binding.cbCar.isChecked) {
            vehicleTypes.add(Constants.VEHICLE_CAR)
        }

        if (binding.cbTruck.isChecked) {
            vehicleTypes.add(Constants.VEHICLE_TRUCK)
        }

        if (vehicleTypes.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one vehicle type", Toast.LENGTH_SHORT).show()
            return
        }

        if (
            fullName.isEmpty() ||
            phone.isEmpty() ||
            address.isEmpty() ||
            vehicleNumber.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceMode = when (serviceModeValue) {
            Constants.SERVICE_BOTH -> listOf(
                Constants.SERVICE_WITHIN_CITY,
                Constants.SERVICE_INTERCITY
            )
            else -> listOf(serviceModeValue)
        }

        profileRepository.updateDriverProfile(
            fullName = fullName,
            phone = phone,
            address = address,
            vehicleTypes = vehicleTypes,
            vehicleNumber = vehicleNumber,
            serviceMode = serviceMode,
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
        binding.cbBike.isEnabled = enabled
        binding.cbCar.isEnabled = enabled
        binding.cbTruck.isEnabled = enabled
        binding.spServiceMode.isEnabled = enabled
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
        originalVehicleNumber = binding.etVehicleNumber.text.toString()
        originalBikeChecked = binding.cbBike.isChecked
        originalCarChecked = binding.cbCar.isChecked
        originalTruckChecked = binding.cbTruck.isChecked
        originalServiceModeIndex = binding.spServiceMode.selectedItemPosition
    }

    private fun restoreOriginalValues() {
        binding.etFullName.setText(originalFullName)
        binding.etEmail.setText(originalEmail)
        binding.etPhone.setText(originalPhone)
        binding.etAddress.setText(originalAddress)
        binding.etVehicleNumber.setText(originalVehicleNumber)
        binding.cbBike.isChecked = originalBikeChecked
        binding.cbCar.isChecked = originalCarChecked
        binding.cbTruck.isChecked = originalTruckChecked
        binding.spServiceMode.setSelection(originalServiceModeIndex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}