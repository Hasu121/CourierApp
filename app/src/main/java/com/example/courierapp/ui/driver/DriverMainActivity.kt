package com.example.courierapp.ui.driver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.courierapp.R
import com.example.courierapp.databinding.ActivityDriverMainBinding
import com.example.courierapp.ui.driver.home.DriverHomeFragment
import com.example.courierapp.ui.driver.notifications.DriverNotificationsFragment
import com.example.courierapp.ui.driver.profile.DriverProfileFragment

class DriverMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(DriverHomeFragment())
        }

        binding.bottomNavigationDriver.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_driver_home -> {
                    loadFragment(DriverHomeFragment())
                    true
                }
                R.id.menu_driver_notifications -> {
                    loadFragment(DriverNotificationsFragment())
                    true
                }
                R.id.menu_driver_profile -> {
                    loadFragment(DriverProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.driverFragmentContainer, fragment)
            .commit()
    }
}