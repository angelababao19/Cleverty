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
import com.example.cleverty.databinding.ActivitySignupPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignupPage : AppCompatActivity() {

    private lateinit var binding: ActivitySignupPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "GoogleAuth"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    binding.progressBar.visibility = View.GONE
                    Log.w(TAG, "Google sign in failed", e)
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

        binding.signupBtn.setOnClickListener {
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val confirmPassword = binding.signupConfirm.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    binding.progressBar.visibility = View.VISIBLE
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        binding.progressBar.visibility = View.GONE
                        if (it.isSuccessful) {
                            val intent = Intent(this, SignupSuccess::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Invalid email or password. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password does not match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
            }

        }
        binding.logintext.setOnClickListener {
            val loginIntent = Intent(this, LoginPage::class.java)
            startActivity(loginIntent)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Google Sign-In successful: ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, SignupSuccess::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    Log.w(TAG, "signInWithCredentialFailed", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}