package com.example.courierapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courierapp.data.pref.SessionManager
import com.example.courierapp.data.repository.AuthRepository
import com.example.courierapp.databinding.ActivityLoginBinding
import com.example.courierapp.ui.customer.CustomerMainActivity
import com.example.courierapp.ui.driver.DriverMainActivity
import com.example.courierapp.utils.Constants

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authRepository.loginUser(
                email = email,
                password = password,
                onSuccess = { uid, role ->
                    sessionManager.saveUserId(uid)
                    sessionManager.saveRole(role)

                    when (role) {
                        Constants.ROLE_CUSTOMER -> {
                            startActivity(Intent(this, CustomerMainActivity::class.java))
                            finish()
                        }
                        Constants.ROLE_DRIVER -> {
                            startActivity(Intent(this, DriverMainActivity::class.java))
                            finish()
                        }
                        else -> {
                            Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterChoiceActivity::class.java))
        }
    }
}