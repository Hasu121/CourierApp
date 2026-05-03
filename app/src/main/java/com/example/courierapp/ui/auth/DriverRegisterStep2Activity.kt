package com.example.courierapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.R
import com.example.courierapp.data.repository.AuthRepository
import com.example.courierapp.databinding.ActivityDriverRegisterStep2Binding
import com.example.courierapp.utils.Constants

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

        val serviceOptions = listOf(
            Constants.SERVICE_WITHIN_CITY,
            Constants.SERVICE_INTERCITY,
            Constants.SERVICE_BOTH
        )

        binding.spServiceMode.adapter = makeSpinnerAdapter(serviceOptions)

        binding.btnRegisterDriver.setOnClickListener {
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

            val vehicleNumber = binding.etVehicleNumber.text.toString().trim()
            val licenseNumber = binding.etLicenseNumber.text.toString().trim()
            val nidNumber = binding.etNidNumber.text.toString().trim()
            val serviceModeValue = binding.spServiceMode.selectedItem.toString()

            if (vehicleTypes.isEmpty()) {
                Toast.makeText(this, "Please select at least one vehicle type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (
                vehicleNumber.isEmpty() ||
                licenseNumber.isEmpty() ||
                nidNumber.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviceMode = when (serviceModeValue) {
                Constants.SERVICE_BOTH -> listOf(
                    Constants.SERVICE_WITHIN_CITY,
                    Constants.SERVICE_INTERCITY
                )

                else -> listOf(serviceModeValue)
            }

            authRepository.registerDriver(
                fullName = fullName,
                email = email,
                phone = phone,
                address = address,
                password = password,
                vehicleTypes = vehicleTypes,
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
    private fun makeSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(
            this,
            R.layout.item_spinner_selected,
            items
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }
}