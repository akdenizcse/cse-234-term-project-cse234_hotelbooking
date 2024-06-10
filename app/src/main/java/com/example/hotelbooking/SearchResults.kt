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
    private var searchQuery: String? = null
    private var departDate: String? = null
    private var returnDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        searchQuery = intent.getStringExtra("searchQuery")?.trim()?.toLowerCase()
        departDate = intent.getStringExtra("departDate")
        returnDate = intent.getStringExtra("returnDate")

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
                    val hotelId = hotelSnapshot.key ?: ""
                    val hotelName = hotelSnapshot.child("name").getValue(String::class.java)
                    val hotelLocation = hotelSnapshot.child("location").getValue(String::class.java)
                    val hotelImageUrl = hotelSnapshot.child("imageUrl").getValue(String::class.java)

                    if (hotelName != null && hotelLocation != null && hotelImageUrl != null) {
                        if (searchQuery == null || hotelLocation.toLowerCase().contains(searchQuery!!)) {
                            hotelsList.add(Hotel(hotelId, hotelName, hotelLocation, hotelImageUrl))
                        }
                    }
                }
                hotelsList.reverse()
                hotelsList.forEach {
                    addHotelToLayout(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun addHotelToLayout(hotel: Hotel) {
        val hotelLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            contentDescription = "Hotel named ${hotel.name} located at ${hotel.location}"
            setOnClickListener {
                val intent = Intent(this@SearchResults, HotelPage::class.java)
                intent.putExtra("hotelId", hotel.id)
                intent.putExtra("departDate", departDate)
                intent.putExtra("returnDate", returnDate)
                startActivity(intent)
            }
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            )
            Glide.with(this@SearchResults).load(hotel.imageUrl).into(this)
            contentDescription = "Image of ${hotel.name}"
        }

        val nameView = TextView(this).apply {
            text = hotel.name
            textSize = 24f
            setPadding(16, 16, 16, 16)
        }

        val locationView = TextView(this).apply {
            text = hotel.location
            textSize = 18f
            setPadding(16, 16, 16, 16)
        }

        hotelLayout.addView(imageView)
        hotelLayout.addView(nameView)
        hotelLayout.addView(locationView)

        binding.hotelListContainer.addView(hotelLayout, 0)
    }

    data class Hotel(
        val id: String = "",
        val name: String = "",
        val location: String = "",
        val imageUrl: String = ""
    )
}
