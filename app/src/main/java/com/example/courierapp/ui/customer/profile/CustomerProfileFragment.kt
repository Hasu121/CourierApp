package com.example.courierapp.ui.customer.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.databinding.FragmentCustomerProfileBinding
import com.example.courierapp.ui.auth.LoginActivity

class CustomerProfileFragment : Fragment() {

    private var _binding: FragmentCustomerProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

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

        val currentUser = FirebaseRefs.auth.currentUser
        if (currentUser != null) {
            FirebaseRefs.db.collection(FirebaseRefs.USERS)
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    binding.tvFullName.text = "Full Name: ${document.getString("fullName").orEmpty()}"
                    binding.tvEmail.text = "Email: ${document.getString("email").orEmpty()}"
                    binding.tvPhone.text = "Phone: ${document.getString("phone").orEmpty()}"
                    binding.tvAddress.text = "Address: ${document.getString("address").orEmpty()}"
                }
        }

        binding.btnLogout.setOnClickListener {
            FirebaseRefs.auth.signOut()
            sessionManager.clear()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}