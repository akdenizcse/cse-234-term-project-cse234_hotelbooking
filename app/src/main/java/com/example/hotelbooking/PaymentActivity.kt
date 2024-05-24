package com.example.hotelbooking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hotelbooking.databinding.ActivityPaymentBinding
class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.payButton.setOnClickListener {
            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MyTripsActivity::class.java)
            startActivity(intent)
        }

    }
}