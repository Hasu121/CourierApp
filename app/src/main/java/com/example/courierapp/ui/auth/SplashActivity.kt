package com.example.courierapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.databinding.ActivitySplashBinding
import com.example.courierapp.ui.customer.CustomerMainActivity
import com.example.courierapp.ui.driver.DriverMainActivity
import com.example.courierapp.utils.Constants

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.root.postDelayed({
            val currentUser = FirebaseRefs.auth.currentUser

            if (currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@postDelayed
            }

            val uid = currentUser.uid

            FirebaseRefs.db.collection(FirebaseRefs.USERS)
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role").orEmpty()

                    sessionManager.saveUserId(uid)
                    sessionManager.saveRole(role)

                    when (role) {
                        Constants.ROLE_CUSTOMER -> {
                            startActivity(Intent(this, CustomerMainActivity::class.java))
                        }
                        Constants.ROLE_DRIVER -> {
                            startActivity(Intent(this, DriverMainActivity::class.java))
                        }
                        else -> {
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                    }
                    finish()
                }
                .addOnFailureListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        }, 1200)
    }
}