package com.example.courierapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.R
import com.example.courierapp.data.repository.AuthRepository
import com.example.courierapp.databinding.ActivityDriverRegisterStep2Binding

class DriverRegisterStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverRegisterStep2Binding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverRegisterStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.courier_green)
        window.navigationBarColor = getColor(R.color.courier_light_bg)

        val fullName = intent.getStringExtra("fullName").orEmpty()
        val email = intent.getStringExtra("email").orEmpty()
        val phone = intent.getStringExtra("phone").orEmpty()
        val address = intent.getStringExtra("address").orEmpty()
        val password = intent.getStringExtra("password").orEmpty()

        binding.btnRegisterDriver.setOnClickListener {
            val vehicleType = binding.etVehicleType.text.toString().trim()
            val vehicleNumber = binding.etVehicleNumber.text.toString().trim()
            val licenseNumber = binding.etLicenseNumber.text.toString().trim()
            val nidNumber = binding.etNidNumber.text.toString().trim()
            val serviceModeText = binding.etServiceMode.text.toString().trim()

            if (vehicleType.isEmpty() || vehicleNumber.isEmpty() || licenseNumber.isEmpty() || nidNumber.isEmpty() || serviceModeText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviceMode = when (serviceModeText.lowercase()) {
                "both" -> listOf("within_city", "intercity")
                else -> listOf(serviceModeText.lowercase())
            }

            authRepository.registerDriver(
                fullName = fullName,
                email = email,
                phone = phone,
                address = address,
                password = password,
                vehicleType = vehicleType,
                vehicleNumber = vehicleNumber,
                licenseNumber = licenseNumber,
                nidNumber = nidNumber,
                serviceMode = serviceMode,
                onSuccess = {
                    Toast.makeText(this, "Driver registered successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                },
                onFailure = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}