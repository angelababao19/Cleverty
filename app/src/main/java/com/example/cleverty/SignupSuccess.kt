package com.example.cleverty

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cleverty.databinding.ActivitySignupSuccessBinding

class SignupSuccess : AppCompatActivity() {

    private lateinit var binding: ActivitySignupSuccessBinding
    private var enterbtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.termstext.setOnClickListener {
            // Create an Intent to start TermsPage
            val intent = Intent(this, TermsPage::class.java)
            startActivity(intent)
        }

        enterbtn = findViewById<Button?>(R.id.enter_btn)
        enterbtn!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(this@SignupSuccess, Homepage::class.java)
                startActivity(intent)
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}