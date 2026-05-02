package com.example.courierapp.ui.customer.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.data.repository.ProfileRepository
import com.example.courierapp.databinding.FragmentCustomerProfileBinding

class CustomerProfileFragment : Fragment() {

    private var _binding: FragmentCustomerProfileBinding? = null
    private val binding get() = _binding!!

    private val profileRepository = ProfileRepository()

    private var isEditing = false

    private var originalFullName = ""
    private var originalEmail = ""
    private var originalPhone = ""
    private var originalAddress = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEditingEnabled(false)
        loadCustomerProfile()

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

            saveCustomerProfile()
        }
    }

    private fun loadCustomerProfile() {
        profileRepository.getCurrentUserProfile(
            onSuccess = { user ->
                binding.etFullName.setText(user.fullName)
                binding.etEmail.setText(user.email)
                binding.etPhone.setText(user.phone)
                binding.etAddress.setText(user.address)

                binding.tvRating.text =
                    "Rating: ${"%.1f".format(user.ratingAverage)} (${user.ratingCount} reviews)"

                saveOriginalValues()
            },
            onFailure = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveCustomerProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        profileRepository.updateCustomerProfile(
            fullName = fullName,
            phone = phone,
            address = address,
            onSuccess = {
                Toast.makeText(requireContext(), "Customer profile updated", Toast.LENGTH_SHORT).show()
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
    }

    private fun restoreOriginalValues() {
        binding.etFullName.setText(originalFullName)
        binding.etEmail.setText(originalEmail)
        binding.etPhone.setText(originalPhone)
        binding.etAddress.setText(originalAddress)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}