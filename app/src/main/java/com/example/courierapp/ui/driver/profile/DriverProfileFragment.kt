package com.example.courierapp.ui.driver.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.data.repository.DriverRepository
import com.example.courierapp.data.repository.ProfileRepository
import com.example.courierapp.databinding.FragmentDriverProfileBinding
import com.example.courierapp.ui.auth.LoginActivity

class DriverProfileFragment : Fragment() {

    private var _binding: FragmentDriverProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val driverRepository = DriverRepository()
    private val profileRepository = ProfileRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDriverProfile()

        binding.btnSaveProfile.setOnClickListener {
            saveDriverProfile()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseRefs.auth.signOut()
            sessionManager.clear()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun loadDriverProfile() {
        driverRepository.getCurrentDriverUser(
            onSuccess = { user ->
                binding.etFullName.setText(user.fullName)
                binding.etEmail.setText(user.email)
                binding.etPhone.setText(user.phone)
                binding.etAddress.setText(user.address)
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )

        driverRepository.getCurrentDriverProfile(
            onSuccess = { profile ->
                binding.etVehicleType.setText(profile.vehicleType)
                binding.etVehicleNumber.setText(profile.vehicleNumber)
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

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty() || vehicleType.isEmpty() || vehicleNumber.isEmpty()) {
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