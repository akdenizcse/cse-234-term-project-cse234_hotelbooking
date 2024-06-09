package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hotelbooking.databinding.ActivityMyTripsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyTripsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyTripsBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTripsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.backButton.setOnClickListener {
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, Dashboard::class.java))
                    true
                }
                R.id.myTrips -> {
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadBookings()
    }

    private fun loadBookings() {
        val userId = auth.currentUser?.uid
        val bookingsRef = database.reference.child("bookings")

        bookingsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    binding.noBookingsTextView.visibility = View.VISIBLE
                    return
                }

                for (bookingSnapshot in snapshot.children) {
                    val isActive = bookingSnapshot.child("active").getValue(Boolean::class.java) ?: false
                    val hotelId = bookingSnapshot.child("hotelId").getValue(String::class.java) ?: ""
                    val roomId = bookingSnapshot.child("roomId").getValue(String::class.java) ?: ""

                    addBookingToLayout(bookingSnapshot.key ?: "", hotelId, roomId, isActive)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun addBookingToLayout(bookingId: String, hotelId: String, roomId: String, isActive: Boolean) {
        val hotelRef = database.reference.child("hotels").child(hotelId)
        hotelRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hotelName = snapshot.child("name").getValue(String::class.java) ?: ""
                val roomName = snapshot.child("rooms").child(roomId).child("name").getValue(String::class.java) ?: ""
                val bookingDates = "Some date range" // Burada gerçek booking tarihlerini almalısınız

                val bookingView = LayoutInflater.from(this@MyTripsActivity).inflate(R.layout.booking_item, null)
                bookingView.findViewById<TextView>(R.id.hotelNameTextView).text = hotelName
                bookingView.findViewById<TextView>(R.id.roomNameTextView).text = roomName
                bookingView.findViewById<TextView>(R.id.bookingDatesTextView).text = bookingDates

                val statusButton = bookingView.findViewById<Button>(R.id.statusButton)
                val cancelButton = bookingView.findViewById<Button>(R.id.cancelBookingButton)
                val rateButton = bookingView.findViewById<Button>(R.id.rateAndCommentButton)

                // Change status button click listener
                statusButton.setOnClickListener {
                    changeBookingStatus(bookingId, isActive, cancelButton, rateButton, statusButton)
                }

                // Cancel booking button click listener
                cancelButton.setOnClickListener {
                    cancelBooking(bookingId, bookingView)
                }

                // Rate and comment button click listener
                rateButton.setOnClickListener {
                    val intent = Intent(this@MyTripsActivity, CommentActivity::class.java)
                    startActivity(intent)
                }

                // Set initial button visibility and colors
                if (isActive) {
                    cancelButton.visibility = View.VISIBLE
                    cancelButton.setBackgroundColor(ContextCompat.getColor(this@MyTripsActivity, android.R.color.holo_red_dark))
                    statusButton.text = "Change Status"
                    statusButton.setBackgroundColor(ContextCompat.getColor(this@MyTripsActivity, android.R.color.holo_red_dark))
                } else {
                    rateButton.visibility = View.VISIBLE
                    rateButton.setBackgroundColor(ContextCompat.getColor(this@MyTripsActivity, android.R.color.holo_blue_dark))
                    statusButton.text = "Change Status"
                    statusButton.setBackgroundColor(ContextCompat.getColor(this@MyTripsActivity, android.R.color.black))
                }

                binding.profileLl.addView(bookingView)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun cancelBooking(bookingId: String, bookingView: View) {
        val bookingRef = database.reference.child("bookings").child(bookingId)
        bookingRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.profileLl.removeView(bookingView)
            } else {
                // Handle error
            }
        }
    }

    private fun changeBookingStatus(bookingId: String, isActive: Boolean, cancelButton: Button, rateButton: Button, statusButton: Button) {
        val bookingRef = database.reference.child("bookings").child(bookingId)
        bookingRef.child("active").setValue(!isActive).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (!isActive) {
                    rateButton.visibility = View.GONE
                    cancelButton.visibility = View.VISIBLE
                    cancelButton.setBackgroundColor(ContextCompat.getColor(this@MyTripsActivity, android.R.color.holo_red_dark))
                } else {
                    cancelButton.visibility = View.GONE
                    rateButton.visibility = View.VISIBLE
                    rateButton.setBackgroundColor(ContextCompat.getColor(this@MyTripsActivity, android.R.color.holo_blue_dark))
                }
                statusButton.text = "Change Status"
                statusButton.setBackgroundColor(ContextCompat.getColor(this@MyTripsActivity, android.R.color.black))
            } else {
                // Handle error
            }
        }
    }
}
