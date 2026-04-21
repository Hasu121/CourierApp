package com.example.courierapp.ui.driver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.data.firebase.FirebaseRefs
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.databinding.ActivityDriverMainBinding
import com.example.courierapp.ui.auth.LoginActivity

class DriverMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnLogout.setOnClickListener {
            FirebaseRefs.auth.signOut()
            sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}