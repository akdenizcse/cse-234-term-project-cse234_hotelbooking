package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hotelbooking.databinding.ActivitySearchResultsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class SearchResults : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

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
                    startActivity(Intent(this, MyTripsActivity::class.java))
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadSearchResults()
    }

    private fun loadSearchResults() {
        database.reference.child("hotels").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hotelsList = mutableListOf<Hotel>()
                for (hotelSnapshot in snapshot.children) {
                    val hotelName = hotelSnapshot.child("name").getValue(String::class.java)
                    val hotelLocation = hotelSnapshot.child("location").getValue(String::class.java)
                    val hotelImageUrl = hotelSnapshot.child("imageUrl").getValue(String::class.java)

                    if (hotelName != null && hotelLocation != null && hotelImageUrl != null) {
                        hotelsList.add(Hotel(hotelName, hotelLocation, hotelImageUrl))
                    }
                }
                hotelsList.reverse()
                hotelsList.forEach {
                    addHotelToLayout(it.name, it.location, it.imageUrl)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun addHotelToLayout(name: String, location: String, imageUrl: String) {
        val hotelLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            contentDescription = "Hotel named $name located at $location"
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            )
            Glide.with(this@SearchResults).load(imageUrl).into(this)
            contentDescription = "Image of $name"
        }

        val nameView = TextView(this).apply {
            text = name
            textSize = 24f
            setPadding(16, 16, 16, 16)
        }

        val locationView = TextView(this).apply {
            text = location
            textSize = 18f
            setPadding(16, 16, 16, 16)
        }

        hotelLayout.addView(imageView)
        hotelLayout.addView(nameView)
        hotelLayout.addView(locationView)

        binding.hotelListContainer.addView(hotelLayout, 0)
    }

    data class Hotel(
        val name: String = "",
        val location: String = "",
        val imageUrl: String = ""
    )
}
