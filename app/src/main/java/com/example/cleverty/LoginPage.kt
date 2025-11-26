package com.example.cleverty

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cleverty.databinding.ActivityLoginPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class LoginPage : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "GoogleAuthLogin" // Slightly different tag for clarity if needed
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize the ActivityResultLauncher for Google Sign-In (SAME AS SIGNUP PAGE)
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle (Login):" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    binding.progressBar.visibility = View.GONE
                    Log.w(TAG, "Google sign in failed (Login)", e)
                    Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.progressBar.visibility = View.GONE
                Log.w(TAG, "Google sign in cancelled or failed (resultCode: ${result.resultCode})")
            }
        }

        binding.googleBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            signInWithGoogle()
        }

        binding.goBack.setOnClickListener {
            val goBackIntent = Intent(this, MainActivity::class.java)
            startActivity(goBackIntent)
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            val intent = Intent(this, LoginSuccess::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Invalid email or password. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signuptext.setOnClickListener {
            val signupIntent = Intent(this, SignupPage::class.java)
            startActivity(signupIntent)
        }

        binding.forgot.setOnClickListener {
            val forgotIntent = Intent(this, ResetPage::class.java)
            startActivity(forgotIntent)
        }

    }
    private fun signInWithGoogle() { // SAME AS SIGNUP PAGE
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) { // SAME AS SIGNUP PAGE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Google Sign-In successful: ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, LoginSuccess::class.java) // Navigate to success screen
                    startActivity(intent)
                    finishAffinity()
                } else {
                    Log.w(TAG, "signInWithCredentialFailed (Login)", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}