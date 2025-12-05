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
import com.example.cleverty.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupPage : AppCompatActivity() {

    private lateinit var binding: ActivitySignupPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "Auth"
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
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account) // Pass the whole account object
                } catch (e: ApiException) {
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
            finish() // Just finish the activity to go back
        }

        binding.signupBtn.setOnClickListener {
            val email = binding.signupEmail.text.toString().trim()
            val password = binding.signupPassword.text.toString().trim()
            val confirmPassword = binding.signupConfirm.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    binding.progressBar.visibility = View.VISIBLE
                    // --- THIS ENTIRE BLOCK IS THE FIX ---
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = firebaseAuth.currentUser
                            if (firebaseUser != null) {
                                // After successful auth, create the user profile in the Realtime Database
                                // with a default name because email/pass signup has no name.
                                createUserProfileInDatabase(firebaseUser, "New User", email)
                            }
                        } else {
                            binding.progressBar.visibility = View.GONE
                            // Show the actual error message from Firebase
                            Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        // We get the idToken from the GoogleSignInAccount object
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        // For Google Sign-In, we have a name from the Google account, so we use it.
                        // The '?:' is the Elvis operator in Kotlin, a safe way to handle nulls.
                        createUserProfileInDatabase(firebaseUser, account.displayName ?: "New User", account.email!!)
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Log.w(TAG, "signInWithCredentialFailed", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- NEW HELPER METHOD TO AVOID DUPLICATE CODE ---
    // This creates the database entry for BOTH email and Google signups.
    private fun createUserProfileInDatabase(firebaseUser: FirebaseUser, name: String, email: String) {
        val uid = firebaseUser.uid
        // Create a User object with an empty string for the profile image URL initially.
        val user = User(name, email, "")

        // Get a reference to the "users" node in your database
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

        // Save the user object under their unique UID
        databaseReference.child(uid).setValue(user).addOnCompleteListener { dbTask ->
            binding.progressBar.visibility = View.GONE
            if (dbTask.isSuccessful) {
                // Profile was successfully created in the database, now proceed.
                Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignupSuccess::class.java)
                startActivity(intent)
                // Clear all previous activities so the user can't go back to the signup flow.
                finishAffinity()
            } else {
                // Handle the case where the database write fails.
                Toast.makeText(this, "Signup successful, but failed to save profile.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
