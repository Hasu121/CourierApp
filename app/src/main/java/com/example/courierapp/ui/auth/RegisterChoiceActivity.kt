package com.example.courierapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.R
import com.example.courierapp.databinding.ActivityRegisterChoiceBinding

class RegisterChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterChoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.courier_green)
        window.navigationBarColor = getColor(R.color.courier_light_bg)

        binding.btnCustomer.setOnClickListener {
            startActivity(Intent(this, CustomerRegisterActivity::class.java))
        }

        binding.btnDriver.setOnClickListener {
            startActivity(Intent(this, DriverRegisterStep1Activity::class.java))
        }
    }
}