package com.example.courierapp.ui.customer.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.data.repository.ProfileRepository
import com.example.courierapp.databinding.FragmentCustomerProfileBinding
import com.example.courierapp.ui.auth.LoginActivity

class CustomerProfileFragment : Fragment() {

    private var _binding: FragmentCustomerProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val profileRepository = ProfileRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfile()

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseRefs.auth.signOut()
            sessionManager.clear()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun loadProfile() {
        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser != null) {
            FirebaseRefs.db.collection(FirebaseRefs.USERS)
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    binding.etFullName.setText(document.getString("fullName").orEmpty())
                    binding.etEmail.setText(document.getString("email").orEmpty())
                    binding.etPhone.setText(document.getString("phone").orEmpty())
                    binding.etAddress.setText(document.getString("address").orEmpty())
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfile() {
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
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
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