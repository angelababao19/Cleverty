package com.example.cleverty

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cleverty.databinding.ActivityResetSuccessBinding

class ResetSuccess : AppCompatActivity() {
    private lateinit var binding: ActivityResetSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.enterBtn.setOnClickListener {
            val enterIntent = Intent(this, LoginPage::class.java)
            startActivity(enterIntent)
        }

        binding.termstext.setOnClickListener {
            // Create an Intent to start TermsPage
            val intent = Intent(this, TermsPage::class.java)
            startActivity(intent)
        }

    }
}