package com.example.cleverty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cleverty.databinding.ActivityResetPageBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPage : AppCompatActivity() {

    private lateinit var binding: ActivityResetPageBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goBack.setOnClickListener {
            finish()
        }

        binding.termstext.setOnClickListener {
            // Create an Intent to start TermsPage
            val intent = Intent(this, TermsPage::class.java)
            startActivity(intent)
        }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.enterBtn.setOnClickListener {
            val email = binding.EmailAddress.text.toString().trim()

            if (validateInput(email)) {
                sendPasswordResetEmail(email)
            }
        }
    }

    private fun validateInput(email: String): Boolean {
        if (email.isEmpty()) {
            binding.EmailAddress.error = "Email address cannot be empty."
            binding.EmailAddress.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EmailAddress.error = "Please enter a valid email address."
            binding.EmailAddress.requestFocus()
            return false
        }
        return true
    }

    private fun sendPasswordResetEmail(email: String) {
        binding.progressBar.visibility = View.VISIBLE

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent. Check your inbox or spam folder.",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(this, ResetSuccess::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("ResetPassword", "sendPasswordResetEmail:failure", task.exception) // For debugging
                    // Handle common errors
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                            "This email address is not registered."
                        is com.google.firebase.FirebaseNetworkException ->
                            "Network error. Please check your connection."
                        else ->
                            "Failed to send password reset email. ${exception?.localizedMessage ?: "Please try again."}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}