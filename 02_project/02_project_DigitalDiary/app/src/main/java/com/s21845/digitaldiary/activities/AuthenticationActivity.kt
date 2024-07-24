package com.s21845.digitaldiary.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.s21845.digitaldiary.MainActivity
import com.s21845.digitaldiary.databinding.ActivityAuthenticationBinding
import com.s21845.digitaldiary.utils.SharedPrefManager
import com.s21845.digitaldiary.R

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the password is already set
        if (!SharedPrefManager.isPasswordSet(this)) {
            // If not, prompt the user to set a password
            binding.tvPrompt.text = getString(R.string.set_your_password)
            binding.btnAuthenticate.text = getString(R.string.set_password)
        }

        binding.btnAuthenticate.setOnClickListener {
            val enteredPassword = binding.etPassword.text.toString()
            if (SharedPrefManager.isPasswordSet(this)) {
                val storedPassword = SharedPrefManager.getPassword(this)
                if (enteredPassword == storedPassword) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.incorrect_password_or_pin), Toast.LENGTH_SHORT).show()
                }
            } else {
                SharedPrefManager.setPassword(this, enteredPassword)
                Toast.makeText(this, getString(R.string.password_set_successfully), Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
