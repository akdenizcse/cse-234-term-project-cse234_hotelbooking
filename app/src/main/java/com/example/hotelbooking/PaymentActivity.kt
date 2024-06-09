package com.example.hotelbooking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hotelbooking.databinding.ActivityPaymentBinding
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var hotelId: String
    private lateinit var roomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.payButton.setOnClickListener {
            // Rezervasyonu kaydet
            saveBookingToDatabase()
        }

        // Kullanıcının seçtiği otel ve oda bilgilerini al
        hotelId = intent.getStringExtra("hotelId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""
    }

    private fun saveBookingToDatabase() {
        val userId = currentUser.uid
        val bookingsRef = database.reference.child("bookings")
        val bookingId = bookingsRef.push().key ?: ""

        val bookingData = HashMap<String, Any>()
        bookingData["userId"] = userId
        bookingData["hotelId"] = hotelId // Doğru otel ID'sini al
        bookingData["roomId"] = roomId // Doğru oda ID'sini al
        bookingData["active"] = true // Varsayılan olarak rezervasyon aktif

        bookingsRef.child(bookingId).setValue(bookingData)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MyTripsActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Booking Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
